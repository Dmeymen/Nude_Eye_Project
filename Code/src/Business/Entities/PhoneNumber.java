package Business.Entities;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a phone number composed of a country prefix and a local number.
 */
public class PhoneNumber {

    @SerializedName(value = "country_prefix", alternate = "countryPrefix")
    private String countryPrefix;

    @SerializedName("number")
    private String number;

    /**
     * Constructs a {@code PhoneNumber} with the given local number and country prefix.
     *
     * @param number        the local phone number digits
     * @param countryPrefix the international prefix (e.g. {@code "34"} for Spain)
     */
    public PhoneNumber(String number, String countryPrefix) {
        this.number = number;
        this.countryPrefix = countryPrefix;
    }

    /**
     * Returns the international prefix for this phone number.
     *
     * @return the country prefix (e.g. {@code "34"})
     */
    public String getCountryPrefix() { return this.countryPrefix; }

    /**
     * Returns the local phone number digits.
     *
     * @return the phone number
     */
    public String getPhoneNumber() { return this.number; }
}
