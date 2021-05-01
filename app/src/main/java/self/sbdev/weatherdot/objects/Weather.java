package self.sbdev.weatherdot.objects;

public class Weather {

    private String date;
    private String icon;
    private String condition;
    private String temp_high_low;
    private String precipitation;
    private String humidity;
    private String cloudiness;
    private String wind_speed;

    public Weather(String date, String icon, String cond, String high_low, String precip, String humid, String cloud, String wind) {
        this.date = date;
        this.icon = icon;
        this.condition = cond;
        this.temp_high_low = high_low;
        this.precipitation = precip;
        this.humidity = humid;
        this.cloudiness = cloud;
        this.wind_speed = wind;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getTemp_high_low() {
        return temp_high_low;
    }

    public void setTemp_high_low(String temp_high_low) {
        this.temp_high_low = temp_high_low;
    }

    public String getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(String precipitation) {
        this.precipitation = precipitation;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getCloudiness() {
        return cloudiness;
    }

    public void setCloudiness(String cloudiness) {
        this.cloudiness = cloudiness;
    }

    public String getWind_speed() {
        return wind_speed;
    }

    public void setWind_speed(String wind_speed) {
        this.wind_speed = wind_speed;
    }
}
