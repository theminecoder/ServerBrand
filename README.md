# ServerBrand

In minecraft 1.13, the server brand is now shown in the client within the debug menu (as well as in crash reports as was from older versions).
This can be useful for server owners to display custom data (such as server ids, game release versions) so that this data can be captured in screenshots + crash reports.

This plugin provides a safe way to set the brand displayed to the user.

## How to use
### Config
```yaml
# The string to display in the debug menu.
# On bungeecord you can use %%server%% to insert the brand coming from the backend server, eg to show the instance id
brand: "My Server"
```

### API
#### Bukkit/Spigot/Paper
```java

import org.bukkit.plugin.java.JavaPlugin;
import me.theminecoder.minecraft.serverbrand.ServerBrandAPI;

public class MyPlugin extends JavaPlugin {
    
    public void onEnable() {
        ServerBrandAPI.getInstance().setBrand("My Custom Brand");
    }
    
}
```

#### Bungeecord/Waterfall
```java

import net.md_5.bungee.api.plugin.Plugin;
import me.theminecoder.minecraft.serverbrand.ServerBrandAPI;

public class MyBungeePlugin extends Plugin {
    
    public void onEnable() {
        ServerBrandAPI.getInstance().setBrand("My Custom Bungee Brand -> %%server%%");
    }
    
}

```