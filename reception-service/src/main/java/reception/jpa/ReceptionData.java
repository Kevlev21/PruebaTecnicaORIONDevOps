package reception.jpa;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reception_data")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceptionData {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

}
