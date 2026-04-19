package com.CampusHub.scheduling_Service.config;

import com.CampusHub.scheduling_Service.entity.Subject;
import com.CampusHub.scheduling_Service.repository.SubjectRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

@Configuration
@Slf4j
public class SubjectDataInitializer {

    @Bean
    CommandLineRunner initSubjects(SubjectRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                log.info("Chargement des matières depuis subjects.json...");
                ObjectMapper mapper = new ObjectMapper();
                try {
                    InputStream inputStream = new ClassPathResource("/subjects.json").getInputStream();
                    JsonNode rootNode = mapper.readTree(inputStream);
                    
                    // La structure est { "niveau": { "1": { "s1": { "subjects": [...] } } } }
                    JsonNode niveauxNode = rootNode.get("niveau");
                    if (niveauxNode != null) {
                        Iterator<Map.Entry<String, JsonNode>> niveaux = niveauxNode.fields();
                        while (niveaux.hasNext()) {
                            Map.Entry<String, JsonNode> niveauEntry = niveaux.next();
                            int niveauId = Integer.parseInt(niveauEntry.getKey());
                            
                            JsonNode semestresNode = niveauEntry.getValue();
                            Iterator<Map.Entry<String, JsonNode>> semestres = semestresNode.fields();
                            while (semestres.hasNext()) {
                                Map.Entry<String, JsonNode> semestreEntry = semestres.next();
                                // Extraction du numéro de semestre (ex: "s1" -> 1)
                                int semestreId = Integer.parseInt(semestreEntry.getKey().replace("s", ""));
                                
                                JsonNode subjectsArray = semestreEntry.getValue().get("subjects");
                                if (subjectsArray != null && subjectsArray.isArray()) {
                                    for (JsonNode subNode : subjectsArray) {
                                        Subject subject = new Subject();
                                        subject.setCode(subNode.get("code").asText());
                                        
                                        // Gestion du nom qui peut être un tableau ou un string dans votre JSON
                                        JsonNode nameNode = subNode.get("name");
                                        if (nameNode.isArray()) {
                                            subject.setName(nameNode.get(0).asText());
                                        } else {
                                            subject.setName(nameNode.asText());
                                        }
                                        
                                        subject.setCredits(subNode.get("credit").asInt());
                                        subject.setCategory(subNode.get("category").asText());
                                        subject.setNiveau(niveauId);
                                        subject.setSemester(semestreId);
                                        
                                        if (subNode.has("specialite")) {
                                            subject.setSpecialite(subNode.get("specialite").asText());
                                        }
                                        
                                        repository.save(subject);
                                    }
                                }
                            }
                        }
                    }
                    log.info("Importation des matières terminée !");
                } catch (Exception e) {
                    log.error("Erreur lors de l'importation des matières: {}", e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }
}
