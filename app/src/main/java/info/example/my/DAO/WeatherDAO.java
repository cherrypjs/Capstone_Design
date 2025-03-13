package info.example.my.DAO;

import android.util.Log;

public class WeatherDAO {

    private String sky;
    private String rain;
    private String temperature;
    private String wind;
    private String wav;
    private String pop;
    private String snow;

    public WeatherDAO() {
    }

    // 생성자
    public WeatherDAO( String sky, String rain, String temperature, String wind, String wav, String pop, String snow) {

        this.sky = sky;
        this.rain = rain;
        this.temperature = temperature;
        this.wind = wind;
        this.wav = wav;
        this.pop = pop;
        this.snow = snow;
    }

    public String getSky() {
        return sky;
    }

    public void setSky(String sky) {
        this.sky = sky;
    }

    public String getRain() {
        return rain;
    }

    public void setRain(String rain) {
        this.rain = rain;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public String getWav() {
        return wav;
    }

    public void setWav(String wav) {
        this.wav = wav;
    }

    public String getPop() {
        return pop;
    }

    public void setPop(String pop) {
        this.pop = pop;
    }

    public String getSnow() {
        return snow;
    }

    public void setSnow(String snow) {
        this.snow = snow;
    }

    @Override
    public String toString() {
        return "Sky: " + sky +
                "#Rain: " + rain +
                "#Temperature: " + temperature +
                "#Wind: " + wind +
                "#Wave: " + wav +
                "#Rain Probability: " + pop +
                "#Snow: " + snow ;

    }
}
