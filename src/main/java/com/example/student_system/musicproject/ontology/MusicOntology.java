package com.example.student_system.musicproject.ontology;

import lombok.Getter;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.XSD;

import java.io.InputStream;
import java.util.*;

@Getter
public class MusicOntology {

    private static final String FILE_PATH = "music.rdf";
    private static final String NS = "http://www.semanticweb.org/emanuilpavlov/ontologies/2026/4/music#";
    private final Map<String, BPMRange> tempoBPMRanges = new HashMap<>();

    private final Map<String, List<String>> moodGenres = new HashMap<>();
    private final Map<String, List<String>> moodTempos = new HashMap<>();
    private final Map<String, List<String>> conditionSymptoms = new HashMap<>();
    private final Map<String, List<String>> effectGenres = new HashMap<>();
    private final Map<String, List<String>> effectTempos = new HashMap<>();
    private final Map<String, List<String>> conditionEffects = new HashMap<>();

    private OntModel model;

    private Property WITH_RESTRICTIONS;
    private Property MIN_INCLUSIVE;
    private Property MAX_EXCLUSIVE;

    public MusicOntology() {
        init();
    }

    private void init() {
        loadOntology();
        initCustomProperties();
        loadTempoDefinitions();
        loadMoodDefinitions();
        loadConditionDefinitions();
        loadEffectDefinitions();
        loadSongConditionDefinitions();
    }

    private void loadOntology() {
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(FILE_PATH)) {
            if (in == null) {
                throw new IllegalStateException("Ontology file not found: " + FILE_PATH);
            }
            model.read(in, null, "TURTLE");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load ontology", e);
        }
    }

    private void initCustomProperties() {
        WITH_RESTRICTIONS = model.createProperty(OWL.NS + "withRestrictions");
        MIN_INCLUSIVE = model.createProperty(XSD.NS + "minInclusive");
        MAX_EXCLUSIVE = model.createProperty(XSD.NS + "maxExclusive");
    }

    private List<OntClass> listAllSubClasses(OntClass root) {
        List<OntClass> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        Deque<OntClass> queue = new ArrayDeque<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            OntClass current = queue.poll();
            current.listSubClasses(true).forEachRemaining(sub -> {
                String uri = sub.getURI();
                if (uri != null && seen.add(uri)) {
                    result.add(sub);
                    queue.add(sub);
                }
            });
        }

        return result;
    }

    private void loadTempoDefinitions() {
        OntClass tempoClass = model.getOntClass(NS + "SongAccordingToTempo");
        if (tempoClass == null) return;

        for (OntClass tempo : listAllSubClasses(tempoClass)) {
            String name = tempo.getLocalName();
            if (name == null) continue;

            tempo.listEquivalentClasses().forEachRemaining(eq -> {
                BPMRange bpmRange = extractBpmRange(eq);
                if (bpmRange != null) tempoBPMRanges.put(name, bpmRange);
            });
        }
    }

    private void loadMoodDefinitions() {
        OntClass emotionClass = model.getOntClass(NS + "Emotion");
        if (emotionClass == null) return;

        for (OntClass mood : listAllSubClasses(emotionClass)) {
            String moodName = mood.getLocalName();
            if (moodName == null || moodName.equals("Emotion")) continue;

            List<String> genres = new ArrayList<>();
            mood.listEquivalentClasses().forEachRemaining(eq -> genres.addAll(extractGenres(eq)));
            if (!genres.isEmpty()) moodGenres.put(moodName, genres);

            List<String> tempos = extractRestrictionTargetsForClass(mood, "hasTempo");
            if (!tempos.isEmpty()) moodTempos.put(moodName, tempos);
        }
    }

    private void loadConditionDefinitions() {
        OntClass conditionClass = model.getOntClass(NS + "MedicalCondition");
        if (conditionClass == null) return;

        for (OntClass condition : listAllSubClasses(conditionClass)) {
            String name = condition.getLocalName();
            if (name == null) continue;

            List<String> symptoms = extractRestrictionTargetsForClass(condition, "hasSymptom");
            if (!symptoms.isEmpty()) conditionSymptoms.put(name, symptoms);
        }
    }

    private void loadEffectDefinitions() {
        OntClass effectClass = model.getOntClass(NS + "Effect");
        if (effectClass == null) return;

        for (OntClass effect : listAllSubClasses(effectClass)) {
            String name = effect.getLocalName();
            if (name == null) continue;

            List<String> genres = new ArrayList<>();
            effect.listEquivalentClasses().forEachRemaining(eq -> genres.addAll(extractGenres(eq)));
            if (!genres.isEmpty()) effectGenres.put(name, genres);

            List<String> tempos = extractRestrictionTargetsForClass(effect, "hasTempo");
            if (!tempos.isEmpty()) effectTempos.put(name, tempos);
        }
    }

    private void loadSongConditionDefinitions() {
        OntClass songConditionClass = model.getOntClass(NS + "SongAccordingToCondition");
        if (songConditionClass == null) return;

        for (OntClass songClass : listAllSubClasses(songConditionClass)) {
            List<String> effects = extractRestrictionTargetsForClass(songClass, "hasEffect");
            List<String> conditions = extractRestrictionTargetsForClass(songClass, "hasMedicalCondition");

            if (effects.isEmpty() || conditions.isEmpty()) continue;

            for (String condition : conditions) {
                conditionEffects
                        .computeIfAbsent(condition, k -> new ArrayList<>())
                        .addAll(effects);
            }
        }
    }

    private BPMRange extractBpmRange(Resource equivalentClass) {
        Statement intersectionStmt = equivalentClass.getProperty(OWL.intersectionOf);
        if (intersectionStmt == null) return null;

        RDFList list = intersectionStmt.getObject().asResource().as(RDFList.class);

        Integer min = null;
        Integer max = null;

        for (RDFNode node : list.asJavaList()) {
            if (!node.isResource()) continue;

            Resource resource = node.asResource();
            Statement onPropertyStmt = resource.getProperty(OWL.onProperty);
            if (onPropertyStmt == null) continue;

            if (!"hasBPM".equals(onPropertyStmt.getObject().asResource().getLocalName())) continue;

            Statement someValuesStmt = resource.getProperty(OWL.someValuesFrom);
            if (someValuesStmt == null) continue;

            Resource datatype = someValuesStmt.getObject().asResource();
            Statement restrictionsStmt = datatype.getProperty(WITH_RESTRICTIONS);
            if (restrictionsStmt == null) continue;

            RDFList restrictions = restrictionsStmt.getObject().asResource().as(RDFList.class);

            for (RDFNode restrictionNode : restrictions.asJavaList()) {
                if (!restrictionNode.isResource()) continue;

                Resource restriction = restrictionNode.asResource();

                Statement minStmt = restriction.getProperty(MIN_INCLUSIVE);
                Statement maxStmt = restriction.getProperty(MAX_EXCLUSIVE);

                if (minStmt != null) min = minStmt.getInt();
                if (maxStmt != null) max = maxStmt.getInt();
            }
        }

        return (min != null && max != null) ? new BPMRange(min, max) : null;
    }

    private List<String> extractGenres(Resource equivalentClass) {
        Statement intersectionStmt = equivalentClass.getProperty(OWL.intersectionOf);
        if (intersectionStmt == null) return List.of();

        RDFList list = intersectionStmt.getObject().asResource().as(RDFList.class);

        Set<String> genres = new LinkedHashSet<>();

        for (RDFNode node : list.asJavaList()) {
            if (!node.isResource()) continue;

            Resource resource = node.asResource();
            Statement unionStmt = resource.getProperty(OWL.unionOf);
            if (unionStmt == null) continue;

            RDFList unionList = unionStmt.getObject().asResource().as(RDFList.class);

            for (RDFNode unionNode : unionList.asJavaList()) {
                if (!unionNode.isResource()) continue;

                Resource restriction = unionNode.asResource();
                Statement propertyStmt = restriction.getProperty(OWL.onProperty);
                if (propertyStmt == null) continue;

                if (!"hasGenre".equals(propertyStmt.getObject().asResource().getLocalName())) continue;

                Statement genreStmt = restriction.getProperty(OWL.someValuesFrom);
                if (genreStmt == null) continue;

                String genreName = genreStmt.getObject().asResource().getLocalName();
                if (genreName != null) genres.add(genreName);
            }
        }

        return new ArrayList<>(genres);
    }

    private List<String> extractRestrictionTargetsForClass(OntClass cls, String propertyLocalName) {
        Set<String> targets = new LinkedHashSet<>();

        cls.listEquivalentClasses().forEachRemaining(eq ->
                targets.addAll(extractRestrictionTargetsFromIntersection(eq, propertyLocalName)));

        targets.addAll(extractRestrictionTargetsFromSuperclasses(cls, propertyLocalName));

        return new ArrayList<>(targets);
    }

    private List<String> extractRestrictionTargetsFromIntersection(Resource equivalentClass, String propertyLocalName) {
        Statement intersectionStmt = equivalentClass.getProperty(OWL.intersectionOf);
        if (intersectionStmt == null) return List.of();

        RDFList list = intersectionStmt.getObject().asResource().as(RDFList.class);
        List<String> targets = new ArrayList<>();

        for (RDFNode node : list.asJavaList()) {
            if (!node.isResource()) continue;
            Resource resource = node.asResource();

            collectIfMatchingRestriction(resource, propertyLocalName, targets);

            Statement unionStmt = resource.getProperty(OWL.unionOf);
            if (unionStmt != null) {
                RDFList unionList = unionStmt.getObject().asResource().as(RDFList.class);
                for (RDFNode unionNode : unionList.asJavaList()) {
                    if (unionNode.isResource()) {
                        collectIfMatchingRestriction(unionNode.asResource(), propertyLocalName, targets);
                    }
                }
            }
        }

        return targets;
    }

    private void collectIfMatchingRestriction(Resource resource, String propertyLocalName, List<String> targets) {
        Statement onPropertyStmt = resource.getProperty(OWL.onProperty);
        if (onPropertyStmt == null) return;
        if (!propertyLocalName.equals(onPropertyStmt.getObject().asResource().getLocalName())) return;

        Statement someValuesStmt = resource.getProperty(OWL.someValuesFrom);
        if (someValuesStmt == null) return;

        String name = someValuesStmt.getObject().asResource().getLocalName();
        if (name != null) targets.add(name);
    }

    private List<String> extractRestrictionTargetsFromSuperclasses(OntClass cls, String propertyLocalName) {
        List<String> targets = new ArrayList<>();

        cls.listSuperClasses(true).forEachRemaining(sup -> {
            if (!sup.isRestriction()) return;

            Restriction restriction = sup.asRestriction();
            if (!restriction.isSomeValuesFromRestriction()) return;

            OntProperty onProperty = restriction.getOnProperty();
            if (onProperty == null || !propertyLocalName.equals(onProperty.getLocalName())) return;

            Resource target = restriction.asSomeValuesFromRestriction().getSomeValuesFrom().asResource();
            String name = target.getLocalName();
            if (name != null) targets.add(name);
        });

        return targets;
    }

    private List<BPMRange> resolveBpmRanges(List<String> tempoNames) {
        List<BPMRange> ranges = new ArrayList<>();
        for (String tempoName : tempoNames) {
            BPMRange range = tempoBPMRanges.get(tempoName);
            if (range != null) ranges.add(range);
        }
        return ranges;
    }

    public List<String> getGenresForMood(String mood) {
        return moodGenres.getOrDefault(mood, Collections.emptyList());
    }

    public List<String> getTemposForMood(String mood) {
        return moodTempos.getOrDefault(mood, Collections.emptyList());
    }

    public List<BPMRange> getBPMRangesForMood(String mood) {
        return resolveBpmRanges(getTemposForMood(mood));
    }

    public boolean isBPMInMoodRange(int bpm, String mood) {
        return getBPMRangesForMood(mood).stream().anyMatch(r -> bpm >= r.min() && bpm < r.max());
    }

    public List<String> getSymptomsForCondition(String condition) {
        return conditionSymptoms.getOrDefault(condition, Collections.emptyList());
    }

    public List<String> getEffectsForCondition(String condition) {
        return conditionEffects.getOrDefault(condition, Collections.emptyList());
    }

    public List<String> getGenresForEffect(String effect) {
        return effectGenres.getOrDefault(effect, Collections.emptyList());
    }

    public List<String> getTemposForEffect(String effect) {
        return effectTempos.getOrDefault(effect, Collections.emptyList());
    }

    public List<BPMRange> getBPMRangesForEffect(String effect) {
        return resolveBpmRanges(getTemposForEffect(effect));
    }

    public boolean isBPMInEffectRange(int bpm, String effect) {
        return getBPMRangesForEffect(effect).stream().anyMatch(r -> bpm >= r.min() && bpm < r.max());
    }

    public record BPMRange(int min, int max) {
        public boolean contains(int bpm) {
            return bpm >= min && bpm < max;
        }
    }
}