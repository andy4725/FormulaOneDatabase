public class Circuit {
    private int circuitId;
    private String name;
    private String country;
    private float length;
    private int raceLaps;
    private float qualiBenchmarkTime;
    private float raceBenchmarkTime;

    public Circuit(int circuitId, String name, String country, float length, int raceLaps, float qualiBenchmarkTime, float raceBenchmarkTime) {
        this.circuitId = circuitId;
        this.name = name;
        this.country = country;
        this.length = length;
        this.raceLaps = raceLaps;
        this.qualiBenchmarkTime = qualiBenchmarkTime;
        this.raceBenchmarkTime = raceBenchmarkTime;
    }

    public int getCircuitId() {
        return circuitId;
    }
    
    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public float getLength() {
        return length;
    }

    public int getRaceLaps() {
        return raceLaps;
    }

    public float getQualiBenchmarkTime() {
        return qualiBenchmarkTime;
    }

    public float getRaceBenchmarkTime() {
        return raceBenchmarkTime;
    }
}
