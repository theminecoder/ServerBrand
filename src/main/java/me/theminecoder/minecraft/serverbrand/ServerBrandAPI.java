package me.theminecoder.minecraft.serverbrand;

/**
 * @author theminecoder
 */
@SuppressWarnings("WeakerAccess")
public final class ServerBrandAPI {

    private static ServerBrandAPI instance = new ServerBrandAPI();

    public static ServerBrandAPI getInstance() {
        return instance;
    }

    private String brand;
    private ServerBrandPlugin plugin;

    private ServerBrandAPI() {

    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
        plugin.updateEveryonesBrand();
    }

    public void setPlugin(ServerBrandPlugin plugin) {
        if (this.plugin != null) {
            throw new IllegalStateException("Cannot refine brand plugin");
        }

        this.plugin = plugin;
    }
}
