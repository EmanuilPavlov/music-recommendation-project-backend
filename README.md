# Music Recommendation Backend

Backend service for a music recommendation system that suggests tracks based on a user's mood or medical condition.
The application uses a multi-agent system with JADE, an ontology for recommendation rules, Firebase Authentication, MySQL for user data and search history, and Audius for music data.

## Technologies

- Java 17
- Spring Boot
- Spring Security
- JADE
- Apache Jena
- Firebase Admin SDK
- MySQL
- Audius API
- Gradle
- JUnit 5
- Mockito

## Main Features

- Firebase token authentication
- Email/password and Google authenticated users
- Mood-based music recommendations
- Medical condition-based recommendations
- Ontology-based BPM, genre, effect and symptom matching
- Audius track retrieval
- User search history
- Multi-agent communication using JADE
- Duplicate track filtering
- Error handling for failed or empty recommendation requests

## Agents

The recommendation process is handled by several JADE agents.

### BridgeAgent

Connects the REST API with the JADE agents and manages recommendation requests.

### MoodAgent

Processes mood requests and uses the ontology to determine suitable music characteristics.

### MedicalConditionAgent

Processes medical condition requests and returns additional information such as:

- matched effect
- related symptoms
- suitable BPM ranges
- suitable genres

## Recommendation Flow

A typical request follows this flow:

```text
Frontend
   |
Spring REST API
   |
BridgeAgent
   |
MoodAgent / MedicalConditionAgent
   |
Ontology
   |
Audius API
   |
Recommendation response
```

## Authentication

The backend verifies Firebase ID tokens sent by the frontend.

Authenticated requests use:
```text
Authorization: Bearer <firebase-id-token>
```

The backend creates a local user record the first time a Firebase user signs in.

## Main Endpoints

### Authentication

```text
POST /api/auth/sign-in
POST /api/auth/logout
```

### Recommendations

```text
GET /api/recommend/by-mood
GET /api/recommend/by-medicalCondition
```

### Search History

```text
GET /api/search-history
```

The authenticated Firebase UID is used automatically for user-specific operations.

## Recommendation Response

Mood recommendations contain information such as:

```json
{
  "title": "Track Name",
  "artist": "Artist",
  "bpm": 90,
  "artwork": "...",
  "audioUrl": "...",
  "duration": 180,
  "mood": "Sad"
}
```

Medical condition recommendations also contain:

```json
{
  "condition": "Insomnia",
  "matchedEffect": "RelaxationEffect",
  "relatedSymptoms": [
    "SleepDifficulty",
    "Restlessness"
  ]
}
```

## Configuration

Before running the application, configure:

- MySQL connection
- Firebase Admin credentials
- Firebase project configuration

Keep Firebase service account files and database credentials outside Git.

## Running the Project

From the project directory:
```bash
./gradlew bootRun
```

On Windows:
```powershell
.\gradlew bootRun
```

The backend runs by default on:
```text
http://localhost:8080
```

## Running Tests

```bash
./gradlew test
```

## Notes

The ontology defines the relationships between moods, medical conditions, symptoms, effects, genres and BPM ranges.
Audius is used as the external music source. The backend filters and removes duplicate tracks before returning recommendations.
The requested recommendation limit is treated as the maximum number of results. The final number may be smaller if there are not enough tracks matching the recommendation criteria.
