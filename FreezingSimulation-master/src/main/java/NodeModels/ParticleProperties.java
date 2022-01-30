package NodeModels;

import java.math.BigDecimal;
import java.util.List;

public class ParticleProperties {

    int velocity;
    List<BigDecimal> positionVector;
    int x;
    int y;
    String myName;
    ParticleProperties p_best_properties;

    public ParticleProperties(int velocity, List<BigDecimal> positionVector) {
        this.velocity = velocity;
        this.positionVector = positionVector;
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }

    public String getMyName() {
        return myName;
    }

    public ParticleProperties() {
    }

    public List<BigDecimal> getPositionVector() {
        return positionVector;
    }

    public void setP_best_properties(ParticleProperties p_best_properties) {
        this.p_best_properties = p_best_properties;
    }

    public ParticleProperties getP_best_properties() {
        return p_best_properties;
    }

    public int getVelocity() {
        return velocity;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
