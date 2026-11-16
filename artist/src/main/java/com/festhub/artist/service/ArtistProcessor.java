import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ArtistProcessor {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public ArtistProcessor(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate(); // Standard Spring HTTP client
    }

    @RabbitListener(queues = "artist.inbound.q")
    public void processArtistEvent(String rawJsonPayload) {
        try {
            // 1. Parse the incoming JSON from the publisher
            JsonNode eventPayload = objectMapper.readTree(rawJsonPayload);
            
            // Extract just the artist portion to send to your API
            // (Adjust the "artist" key to whatever the actual JSON file uses)
            JsonNode artistData = eventPayload.get("artist");

            // 2. Persist the data via your existing microservice REST API
            String apiUrl = "http://localhost:8080/api/artist/create"; 
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(apiUrl, artistData, JsonNode.class);

            // 3. Extract the newly generated UUID from your database response
            String newArtistId = response.getBody().get("id").asText();

            // 4. Inject the new ID into the payload so Stage 2 (Concert Event Processor) can use it
            ((ObjectNode) eventPayload).put("artistId", newArtistId);

            // 5. Emit downstream event on success (To: festhub.events exchange -> artist.registered routing key)
            rabbitTemplate.convertAndSend("festhub.events", "artist.registered", eventPayload.toString());
            
            System.out.println("Success: Artist processed and routed downstream.");

        } catch (Exception e) {
            // 6. Explicitly route to Dead Letter Exchange (DLX) on any validation or API failure
            rabbitTemplate.convertAndSend("festhub.dlx", "artist.failed", rawJsonPayload);
            
            System.err.println("Failed: Invalid artist data routed to DLQ.");
        }
    }
}
