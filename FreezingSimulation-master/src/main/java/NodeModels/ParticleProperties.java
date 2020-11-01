package NodeModels;

import java.math.BigDecimal;
import java.util.List;

public class ParticleProperties {

    BigDecimal velocity;
    List<BigDecimal> positionVector;
    int x;
    int y;
    ParticleProperties p_best_properties;

    public ParticleProperties(BigDecimal velocity, List<BigDecimal> positionVector) {
        this.velocity = velocity;
        this.positionVector = positionVector;
    }

    public ParticleProperties() {
    }

    public BigDecimal getVelocity() {
        return velocity;
    }

    public void setVelocity(BigDecimal velocity) {
        this.velocity = velocity;
    }
}
