package dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ReportResponse {

    private Long id;
    private String name;
    private String type;
    private Instant createdAt;
}
