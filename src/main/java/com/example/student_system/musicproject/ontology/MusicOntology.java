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

    private final Map<String, BPMRange> moodBPMRanges = new HashMap<>();
    private final Map<String, List<String>> moodGenres = new HashMap<>();

    // MedicalCondition -> list of Symptom local names (hasSymptom restrictions)
    private final Map<String, List<String>> conditionSymptoms = new HashMap<>();

    // Effect -> BPMRange / genres, read straight from the Effect class's OWN equivalentClass
    // definition (same intersectionOf(self, unionOf(hasGenre...), hasBPM) shape as Emotion).
    private final Map<String, BPMRange> effectBPMRanges = new HashMap<>();
    private final Map<String, List<String>> effectGenres = new HashMap<>();

    // MedicalCondition -> list of Effect local names, read from the SongAccordingToCondition
    // subclasses (AnxietyReliefSong, DepressionFriendlySong, FocusEnhancementSong,
    // HighPerformanceSong, MeditationSong, SleepAidSong), which are the only place in this
    // ontology that actually links a condition to an effect. There is no Effect->Symptom
    // property anywhere in the ontology, so this replaces the old
    // condition->symptom->effect matching, which could never produce results.
    // NOTE: SleepAidSong has no hasMedicalCondition restriction in the ontology as authored,
    // so Insomnia will not pick up SleepInductionEffect through this map unless that's added.
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

    // model is OWL_DL_MEM with no reasoner attached, so OntClass.listSubClasses() only
    // reliably finds DIRECT children - it does not walk multi-hop rdfs:subClassOf chains.
    // Effect has an extra layer (Effect -> CognitiveEffect/PhysicalEffect/TherapeuticEffect
    // -> the actual effect classes), so the built-in call silently returns zero usable
    // classes for it. This does the traversal manually so it works regardless of depth.
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

    private void loadMoodDefinitions() {
        OntClass emotionClass = model.getOntClass(NS + "Emotion");
        if (emotionClass == null) return;

        for (OntClass mood : listAllSubClasses(emotionClass)) {
            String moodName = mood.getLocalName();
            if (moodName == null || moodName.equals("Emotion")) continue;

            mood.listEquivalentClasses().forEachRemaining(eq -> {
                BPMRange bpmRange = extractBpmRange(eq);
                List<String> genres = extractGenres(eq);

                if (bpmRange != null) moodBPMRanges.put(moodName, bpmRange);
                if (!genres.isEmpty()) moodGenres.put(moodName, genres);
            });
        }
    }

    // MedicalCondition subclasses (ADHD, Anxiety, Burnout, ChronicPain, Dementia, Depression,
    // Dyslexia, Insomnia, Schizophrenia, SevereFatigue) are defined via owl:equivalentClass +
    // owl:intersectionOf of "hasSymptom someValuesFrom X" restrictions, each exposed directly
    // in the intersection list (no unionOf wrapping) - so the plain case (A) path below is
    // all that's needed here.
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

    // Every Effect subclass (AnxietyReductionEffect, RelaxationEffect, ConcentrationBoost,
    // EnergyBoost, SleepInductionEffect, etc.) carries its own equivalentClass definition in
    // the exact same shape as the Emotion classes: intersectionOf(self, unionOf(hasGenre
    // restrictions), hasBPM restriction). So this reuses extractBpmRange/extractGenres
    // directly instead of trying to derive Effect BPM/genre indirectly through
    // SongAccordingToCondition classes or through a Symptom link that doesn't exist.
    private void loadEffectDefinitions() {
        OntClass effectClass = model.getOntClass(NS + "Effect");
        if (effectClass == null) return;

        for (OntClass effect : listAllSubClasses(effectClass)) {
            String name = effect.getLocalName();
            if (name == null) continue;

            effect.listEquivalentClasses().forEachRemaining(eq -> {
                BPMRange bpmRange = extractBpmRange(eq);
                List<String> genres = extractGenres(eq);

                if (bpmRange != null) effectBPMRanges.put(name, bpmRange);
                if (!genres.isEmpty()) effectGenres.put(name, genres);
            });
        }
    }

    // SongAccordingToCondition subclasses are the only place this ontology links a
    // MedicalCondition to an Effect. Both the hasEffect and hasMedicalCondition restrictions
    // can appear either directly in the intersection list (SleepAidSong's hasEffect,
    // AnxietyReliefSong/DepressionFriendlySong's hasMedicalCondition) or wrapped in a unionOf
    // of alternative restrictions (AnxietyReliefSong/DepressionFriendlySong/MeditationSong's
    // hasEffect; FocusEnhancementSong/HighPerformanceSong/MeditationSong's hasMedicalCondition).
    // extractRestrictionTargetsForClass now handles both shapes uniformly.
    private void loadSongConditionDefinitions() {
        OntClass songConditionClass = model.getOntClass(NS + "SongAccordingToCondition");
        if (songConditionClass == null) return;

        for (OntClass songClass : listAllSubClasses(songConditionClass)) {
            List<String> effects = extractRestrictionTargetsForClass(songClass, "hasEffect");
            List<String> conditions = extractRestrictionTargetsForClass(songClass, "hasMedicalCondition");

            // SleepAidSong currently has no hasMedicalCondition restriction in the ontology,
            // so it's skipped here - it never claims a condition to link its effect to.
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

    // Used for Emotion and Effect classes, whose hasGenre restrictions are always exposed as
    // a unionOf several full Restriction objects (each with its own onProperty=hasGenre).
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

    // General-purpose extractor for "does this class require property P to have some value X"
    // restrictions, handling both shapes actually used in this ontology:
    //   (A) the restriction sits directly in the intersectionOf list
    //       e.g. ADHD's hasSymptom restrictions, SleepAidSong's hasEffect restriction
    //   (B) the restriction is one of several alternatives inside a unionOf
    //       e.g. AnxietyReliefSong's hasEffect union, FocusEnhancementSong's
    //       hasMedicalCondition union
    // Also checks plain rdfs:subClassOf restrictions as a fallback in case some class in the
    // ontology is authored that way instead of via equivalentClass.
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

            // Case A: the intersection member IS the restriction.
            collectIfMatchingRestriction(resource, propertyLocalName, targets);

            // Case B: the intersection member is a unionOf several alternative restrictions.
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

    public boolean isBPMInMoodRange(int bpm, String mood) {
        BPMRange range = moodBPMRanges.get(mood);
        return range != null && bpm >= range.min() && bpm < range.max();
    }

    public List<String> getGenresForMood(String mood) {
        return moodGenres.getOrDefault(mood, Collections.emptyList());
    }

    public BPMRange getBPMRangeForMood(String mood) {
        return moodBPMRanges.get(mood);
    }

    public List<String> getSymptomsForCondition(String condition) {
        return conditionSymptoms.getOrDefault(condition, Collections.emptyList());
    }

    /** Condition name straight to the list of relevant Effect names, via SongAccordingToCondition. */
    public List<String> getEffectsForCondition(String condition) {
        return conditionEffects.getOrDefault(condition, Collections.emptyList());
    }

    public BPMRange getBPMRangeForEffect(String effect) {
        return effectBPMRanges.get(effect);
    }

    public List<String> getGenresForEffect(String effect) {
        return effectGenres.getOrDefault(effect, Collections.emptyList());
    }

    public boolean isBPMInEffectRange(int bpm, String effect) {
        BPMRange range = effectBPMRanges.get(effect);
        return range != null && bpm >= range.min() && bpm < range.max();
    }

    public record BPMRange(int min, int max) {}
}