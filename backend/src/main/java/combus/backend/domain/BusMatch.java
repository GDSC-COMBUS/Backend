package combus.backend.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Driver 엔터티와의 관계를 표시하는 필드 추가
    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    private Long busId;
}
