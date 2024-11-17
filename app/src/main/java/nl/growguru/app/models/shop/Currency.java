package nl.growguru.app.models.shop;

/**
 * Three-letter <a href="https://www.iso.org/iso-4217-currency-codes.html">ISO currency code</a>, and in-game LEAFS.
 */
public enum Currency {

    LEAFS,
    EUR;

    public String toLowerCaseString() {
        return this.toString().toLowerCase();
    }
}
