package odm_finance.finance.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplate {
    private String to;
    private String subject;
    private String templateName;
    private Map<String, Object> variables;
    private byte[] attachment;
    private String attachmentName;
    private String attachmentType;
}

