package com.CampusHub.scheduling_Service.config;

import com.CampusHub.scheduling_Service.client.UserClient;
import com.CampusHub.scheduling_Service.client.UserDTO;
import com.CampusHub.scheduling_Service.entity.TeacherAssignment;
import com.CampusHub.scheduling_Service.repository.TeacherAssignmentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class TeacherAssignmentDataInitializer {

    private final UserClient userClient;
    private final TeacherAssignmentRepository repository;

    public TeacherAssignmentDataInitializer(UserClient userClient, TeacherAssignmentRepository repository) {
        this.userClient = userClient;
        this.repository = repository;
    }

    @Bean
    CommandLineRunner initTeacherAssignments() {
        return args -> {
            if (repository.count() == 0) {
                log.info("Démarrage de la résolution dynamique des assignations d'enseignants...");
                
                try {
                    // 1. Récupérer tous les enseignants via Feign
                    List<UserDTO> allUsers = userClient.getAllUsers();
                    Map<String, UserDTO> teacherMap = allUsers.stream()
                            .filter(u -> "TEACHER".equals(u.getRole()))
                            .collect(Collectors.toMap(
                                    u -> normalizeName(u.getFullName()),
                                    u -> u,
                                    (existing, replacement) -> existing // Garder le premier en cas de doublon
                            ));

                    log.info("Résolution: {} enseignants trouvés dans le user-service.", teacherMap.size());

                    // 2. Lire subjects.json pour extraire les liaisons Nom Enseignant <-> Code Matière
                    ObjectMapper mapper = new ObjectMapper();
                    InputStream inputStream = new ClassPathResource("/subjects.json").getInputStream();
                    JsonNode rootNode = mapper.readTree(inputStream);
                    
                    List<TeacherAssignment> assignmentsToSave = new ArrayList<>();
                    
                    JsonNode niveauxNode = rootNode.get("niveau");
                    if (niveauxNode != null) {
                        niveauxNode.fields().forEachRemaining(niveauEntry -> {
                            niveauEntry.getValue().fields().forEachRemaining(semestreEntry -> {
                                JsonNode subjectsArray = semestreEntry.getValue().get("subjects");
                                if (subjectsArray != null && subjectsArray.isArray()) {
                                    for (JsonNode subNode : subjectsArray) {
                                        String subjectCode = subNode.get("code").asText();
                                        
                                        // Lecteurs de cours
                                        processRole(subNode, "Course Lecturer", "COURSE_LECTURER", subjectCode, teacherMap, assignmentsToSave);
                                        // Assistants
                                        processRole(subNode, "Assitant lecturer", "ASSISTANT_LECTURER", subjectCode, teacherMap, assignmentsToSave);
                                        // Cas spécifiques (Assistant 1, Assistant 2)
                                        processRole(subNode, "Assitant lecturer 1", "ASSISTANT_LECTURER", subjectCode, teacherMap, assignmentsToSave);
                                        processRole(subNode, "Assitant lecturer 2", "ASSISTANT_LECTURER", subjectCode, teacherMap, assignmentsToSave);
                                    }
                                }
                            });
                        });
                    }

                    if (!assignmentsToSave.isEmpty()) {
                        repository.saveAll(assignmentsToSave);
                        log.info("Succès: {} assignations d'enseignants créées avec les IDs réels !", assignmentsToSave.size());
                    } else {
                        log.warn("Aucune assignation n'a pu être résolue. Vérifiez la correspondance des noms.");
                    }

                } catch (Exception e) {
                    log.error("Erreur lors de la résolution dynamique des assignations: {}", e.getMessage());
                }
            }
        };
    }

    private void processRole(JsonNode subNode, String jsonKey, String roleLabel, String subjectCode, Map<String, UserDTO> teacherMap, List<TeacherAssignment> list) {
        if (subNode.has(jsonKey)) {
            JsonNode namesNode = subNode.get(jsonKey);
            if (namesNode.isArray()) {
                String fullName = extractFullName(namesNode);
                if (!fullName.isEmpty()) {
                    UserDTO teacher = findTeacher(fullName, teacherMap);
                    if (teacher != null) {
                        list.add(new TeacherAssignment(null, subjectCode, teacher.getId(), roleLabel, teacher.getFullName()));
                    } else {
                        log.debug("Impossible de trouver l'ID pour l'enseignant: {}", fullName);
                    }
                }
            }
        }
    }

    private String extractFullName(JsonNode namesNode) {
        StringBuilder sb = new StringBuilder();
        for (JsonNode n : namesNode) {
            String part = n.asText().trim();
            if (!part.isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(part);
            }
        }
        return sb.toString();
    }

    private String normalizeName(String name) {
        if (name == null) return "";
        return name.trim().toUpperCase().replaceAll("\\s+", " ");
    }

    private UserDTO findTeacher(String name, Map<String, UserDTO> teacherMap) {
        String normalized = normalizeName(name);
        // Correspondance directe
        if (teacherMap.containsKey(normalized)) return teacherMap.get(normalized);
        
        // Inversion (Prénom Nom -> Nom Prénom)
        String[] parts = normalized.split(" ");
        if (parts.length >= 2) {
            String reversed = parts[parts.length - 1] + " " + String.join(" ", Arrays.copyOfRange(parts, 0, parts.length - 1));
            if (teacherMap.containsKey(reversed)) return teacherMap.get(reversed);
        }

        // Fuzzy match simple
        for (Map.Entry<String, UserDTO> entry : teacherMap.entrySet()) {
            if (normalized.contains(entry.getKey()) || entry.getKey().contains(normalized)) {
                return entry.getValue();
            }
        }
        
        return null;
    }
}
