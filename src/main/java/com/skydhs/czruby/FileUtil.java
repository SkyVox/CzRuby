package com.skydhs.czruby;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtil {
    private static FileUtil instance;

    // FileUtil logger.
    private static final Logger logger = Logger.getLogger(FileUtil.class.getCanonicalName());

    /*
     * This is the error message.
     */
    private static final String DEFAULT_FILE_IS_NULL_ERROR = "Default file is null.";

    /*
     * Default file extension.
     * Whenever you give a file without it's
     * extension we'll use this as default.
     */
    private static final String DEFAULT_FILE_EXTENSION = '.' + "yml";

    /*
     * Default file encoding.
     * As default we use "UTF-8" for the support
     * to texts accents.
     */
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /*
     * Default file path.
     * This is the default path where the default
     * file is located on plugin's resources
     * folder.
     * By default we'll copy file from: ../resources/files/%your_file%/.
     */
    private static final String DEFAULT_FILE_PATH = "files" + '/';

    // ----------
    // Private class fields
    // ----------

    private Core plugin;
    private Set<FileManager> loadedFiles;

    public FileUtil(Core plugin, FileInfo[] files) {
        this(plugin, files, DEFAULT_FILE_PATH);
    }

    public FileUtil(Core plugin, FileInfo[] files, final String path) {
        FileUtil.instance = this; // Set the instance as this class.

        // Create a new String[].
        String[] filledPath = new String[files.length];
        Arrays.fill(filledPath, path);

        this.plugin = plugin;
        this.loadFile(files, filledPath);
    }

    public FileUtil(Core plugin, FileInfo[] files, final String[] path) {
        FileUtil.instance = this; // Set the instance as this class.

        this.plugin = plugin;
        this.loadFile(files, path);
    }

    /**
     * Save all configuration files.
     */
    public void saveAll() {
        this.loadedFiles.forEach(FileManager::save);
    }

    /**
     * Load configuration files.
     *
     * @param files File information, we use
     *              those informations to load
     *              the config file.
     * @param path Default file path, we'll use
     *             this path to copy the default
     *             file.
     *             Path should not contains file name.
     */
    public void loadFile(FileInfo[] files, final String[] path) {
        if (files.length != path.length) throw new IllegalArgumentException("Arrays should have the same size.");
        this.loadedFiles = new HashSet<>(files.length);

        for (int i = 0; i < files.length; i++) {
            this.loadedFiles.add(new FileManager(files[i], path[i]));
        }
    }

    /**
     * Load configuration file.
     *
     * @param file File information, we use
     *             those informations to load
     *             the config file.
     */
    public void loadFile(FileInfo file) {
        loadFile(file, DEFAULT_FILE_PATH);
    }

    /**
     * Load configuration file.
     *
     * @param file File information, we use
     *              those informations to load
     *              the config file.
     * @param path Default file path, we'll use
     *             this path to copy the default
     *             file.
     *             Path should not contains file name.
     */
    public void loadFile(FileInfo file, final String path) {
        Set<FileManager> ret = new HashSet<>(this.loadedFiles.size() + 1);
        ret.addAll(loadedFiles);

        // Load the given file.
        ret.add(new FileManager(file, path));

        // Clear old files.
        loadedFiles.clear();

        this.loadedFiles = ret;
    }

    /**
     * Copy default file.
     *
     * @param is Default input stream.
     * @param file File output.
     */
    private static void copyFile(InputStream is, File file) {
        OutputStream out = null;

        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;

            while ((len = is.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "An error has occurred while copying an configuration file.", ex);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                logger.log(Level.WARNING, "An error has occurred while closing OutputStream!", ex);
            }
        }
    }

    /**
     * Set a value on configuration file.
     *
     * @param path Path to be set.
     * @param value This is the value that
     *              will be added on configuration file.
     */
    public void set(final String path, Object value) {
        set(Objects.requireNonNull(getDefaultFile(), DEFAULT_FILE_IS_NULL_ERROR), path, value);
    }

    /**
     * Set a value on configuration file.
     *
     * @param file File to set the given value.
     * @param path Path to be set.
     * @param value This is the value that
     *              will be added on configuration file.
     */
    public void set(FileManager file, final String path, Object value) {
        set(file, path, value, Boolean.TRUE);
    }

    /**
     * Set a value on configuration file.
     *
     * @param file File to set the given value.
     * @param path Path to be set.
     * @param value This is the value that
     *              will be added on configuration file.
     * @param save If we should instantly save this
     *             configuration file.
     */
    public void set(FileManager file, final String path, Object value, Boolean save) {
        file.get().set(path, value);

        if (save) {
            file.save();
        }
    }

    /**
     * Verify if the given path contains
     * on the default file.
     *
     * @param path Path to search.
     * @return If given path is valid |
     *          contains on configuration file.
     */
    public boolean contains(final String path) {
        return Objects.requireNonNull(getDefaultFile(), DEFAULT_FILE_IS_NULL_ERROR).get().contains(path);
    }

    /**
     * Verify if the given path contains
     * on the default file.
     *
     * @param file File to search.
     * @param path Path to search.
     * @return If given path is valid |
     *          contains on configuration file.
     */
    public boolean contains(FileManager file, final String path) {
        return file.get().contains(path);
    }

    /**
     * Get Boolean value from configuration file.
     *
     * @param path Path to search.
     * @return Config value.
     */
    public boolean getBoolean(final String path) {
        return StringUtils.equalsIgnoreCase(Objects.requireNonNull(getDefaultFile(), DEFAULT_FILE_IS_NULL_ERROR).get().getString(path), "true");
    }

    /**
     * Get Boolean value from configuration file.
     *
     * @param path Path to search.
     * @param value This will be used as default value
     *              if the config does not contains the
     *              given path.
     * @return Config value.
     */
    public boolean getBoolean(final String path, Boolean value) {
        return contains(path) ? getBoolean(path) : value;
    }

    /**
     * Get Boolean value from configuration file.
     *
     * @param file File to search.
     * @param path Path to search.
     * @return Config value.
     */
    public boolean getBoolean(FileManager file, final String path) {
        return StringUtils.equalsIgnoreCase(file.get().getString(path), "true");
    }

    /**
     * Get Boolean value from configuration file.
     *
     * @param file File to search.
     * @param path Path to search.
     * @param value This will be used as default value
     *              if the config does not contains the
     *              given path.
     * @return Config value.
     */
    public boolean getBoolean(FileManager file, final String path, Boolean value) {
        return contains(file, path) ? getBoolean(file, path) : value;
    }

    /**
     * Get Integer value from configuration file.
     *
     * @param path Path to search.
     * @return Config value.
     */
    public int getInt(final String path) {
        return Objects.requireNonNull(getDefaultFile(), DEFAULT_FILE_IS_NULL_ERROR).get().getInt(path);
    }

    /**
     * Get Integer value from configuration file.
     *
     * @param path Path to search.
     * @param value This will be used as default value
     *              if the config does not contains the
     *              given path.
     * @return Config value.
     */
    public int getInt(final String path, int value) {
        return contains(path) ? getInt(path) : value;
    }

    /**
     * Get Integer value from configuration file.
     *
     * @param file File to search.
     * @param path Path to search.
     * @return Config value.
     */
    public int getInt(FileManager file, final String path) {
        return file.get().getInt(path);
    }

    /**
     * Get Integer value from configuration file.
     *
     * @param file File to search.
     * @param path Path to search.
     * @param value This will be used as default value
     *              if the config does not contains the
     *              given path.
     * @return Config value.
     */
    public int getInt(FileManager file, final String path, int value) {
        return contains(file, path) ? getInt(file, path) : value;
    }

    /**
     * Get Long value from configuration file.
     *
     * @param path Path to search.
     * @return Config value.
     */
    public long getLong(final String path) {
        return Objects.requireNonNull(getDefaultFile(), DEFAULT_FILE_IS_NULL_ERROR).get().getLong(path);
    }

    /**
     * Get Long value from configuration file.
     *
     * @param path Path to search.
     * @param value This will be used as default value
     *              if the config does not contains the
     *              given path.
     * @return Config value.
     */
    public long getLong(final String path, long value) {
        return contains(path) ? getLong(path) : value;
    }

    /**
     * Get Long value from configuration file.
     *
     * @param file File to search.
     * @param path Path to search.
     * @return Config value.
     */
    public long getLong(FileManager file, final String path) {
        return file.get().getLong(path);
    }

    /**
     * Get Long value from configuration file.
     *
     * @param file File to search.
     * @param path Path to search.
     * @param value This will be used as default value
     *              if the config does not contains the
     *              given path.
     * @return Config value.
     */
    public long getLong(FileManager file, final String path, long value) {
        return contains(file, path) ? getLong(file, path) : value;
    }

    /**
     * Get Double value from configuration file.
     *
     * @param path Path to search.
     * @return Config value.
     */
    public double getDouble(final String path) {
        return Objects.requireNonNull(getDefaultFile(), DEFAULT_FILE_IS_NULL_ERROR).get().getDouble(path);
    }

    /**
     * Get Double value from configuration file.
     *
     * @param path Path to search.
     * @param value This will be used as default value
     *              if the config does not contains the
     *              given path.
     * @return Config value.
     */
    public double getDouble(final String path, double value) {
        return contains(path) ? getDouble(path) : value;
    }

    /**
     * Get Double value from configuration file.
     *
     * @param file File to search.
     * @param path Path to search.
     * @return Config value.
     */
    public double getDouble(FileManager file, final String path) {
        return file.get().getDouble(path);
    }

    /**
     * Get Double value from configuration file.
     *
     * @param file File to search.
     * @param path Path to search.
     * @param value This will be used as default value
     *              if the config does not contains the
     *              given path.
     * @return Config value.
     */
    public double getDouble(FileManager file, final String path, double value) {
        return contains(file, path) ? getDouble(file, path) : value;
    }

    /**
     * Get Float value from configuration file.
     *
     * @param path Path to search.
     * @return Config value.
     */
    public float getFloat(final String path) {
        return (float) getDouble(path);
    }

    /**
     * Get Float value from configuration file.
     *
     * @param path Path to search.
     * @param value This will be used as default value
     *              if the config does not contains the
     *              given path.
     * @return Config value.
     */
    public float getFloat(final String path, float value) {
        return contains(path) ? getFloat(path) : value;
    }

    /**
     * Get Float value from configuration file.
     *
     * @param file File to search.
     * @param path Path to search.
     * @return Config value.
     */
    public float getFloat(FileManager file, final String path) {
        return (float) getDouble(file, path);
    }

    /**
     * Get Float value from configuration file.
     *
     * @param file File to search.
     * @param path Path to search.
     * @param value This will be used as default value
     *              if the config does not contains the
     *              given path.
     * @return Config value.
     */
    public float getFloat(FileManager file, final String path, float value) {
        return contains(file, path) ? getFloat(file, path) : value;
    }

    /**
     * Get a String from configuration file.
     *
     * Using {@link StringReplace} you can replace
     * the current String for placeholders or
     * convert it to set|list and more.
     *
     * @param path Path to search.
     * @param value This will be used as default value
     *              if the config does not contains the
     *              given path. This value can be null.
     * @return New instance of {@link StringReplace}
     */
    public StringReplace getString(final String path, @Nullable String... value) {
        return contains(path) ?
                new StringReplace(Objects.requireNonNull(getDefaultFile(), DEFAULT_FILE_IS_NULL_ERROR).get().getString(path)) :
                new StringReplace(value);
    }

    /**
     * Get a String from configuration file.
     *
     * Using {@link StringReplace} you can replace
     * the current String for placeholders or
     * convert it to set|list and more.
     *
     * @param path Path to search.
     * @param placeholders Placeholder names.
     * @param replacements Replacements to replace the placeholders.
     * @return New instance of {@link StringReplace}
     */
    public StringReplace getString(final String path, String[] placeholders, String[] replacements) {
        return new StringReplace(Objects.requireNonNull(getDefaultFile(), DEFAULT_FILE_IS_NULL_ERROR).get().getString(path)).replace(placeholders, replacements);
    }

    /**
     * Get a String from configuration file.
     *
     * Using {@link StringReplace} you can replace
     * the current String for placeholders or
     * convert it to set|list and more.
     *
     * @param file File to search.
     * @param path Path to search.
     * @param value This will be used as default value
     *              if the config does not contains the
     *              given path. This value can be null.
     * @return New instance of {@link StringReplace}
     */
    public StringReplace getString(FileManager file, final String path, @Nullable String... value) {
        return contains(file, path) ?
                new StringReplace(file.get().getString(path)) :
                new StringReplace(value);
    }

    /**
     * Get a String from configuration file.
     *
     * Using {@link StringReplace} you can replace
     * the current String for placeholders or
     * convert it to set|list and more.
     *
     * @param file File to search.
     * @param path Path to search.
     * @param placeholders Placeholder names.
     * @param replacements Replacements to replace the placeholders.
     * @return New instance of {@link StringReplace}
     */
    public StringReplace getString(FileManager file, final String path, String[] placeholders, String[] replacements) {
        return new StringReplace(file.get().getString(path)).replace(placeholders, replacements);
    }

    /**
     * Get a String List from configuration file.
     *
     * Using {@link StringReplace} you can replace
     * the current String for placeholders or
     * convert it to set|list and more.
     *
     * @param path Path to search.
     * @return New instance of {@link StringReplace}
     */
    public StringReplace getList(final String path) {
        return new StringReplace(Objects.requireNonNull(getDefaultFile(), DEFAULT_FILE_IS_NULL_ERROR).get().getStringList(path));
    }

    /**
     * Get a String List from configuration file.
     *
     * Using {@link StringReplace} you can replace
     * the current String for placeholders or
     * convert it to set|list and more.
     *
     * @param path Path to search.
     * @param value This will be used as default value
     *              if the config does not contains the
     *              given path. This value can be null.
     * @return New instance of {@link StringReplace}
     */
    public StringReplace getList(final String path, List<String> value) {
        return contains(path) ? getList(path) : new StringReplace(value);
    }

    /**
     * Get a String List from configuration file.
     *
     * Using {@link StringReplace} you can replace
     * the current String for placeholders or
     * convert it to set|list and more.
     *
     * @param file File to search.
     * @param path Path to search.
     * @return New instance of {@link StringReplace}
     */
    public StringReplace getList(FileManager file, final String path) {
        return new StringReplace(file.get().getString(path));
    }

    /**
     * Get a String List from configuration file.
     *
     * Using {@link StringReplace} you can replace
     * the current String for placeholders or
     * convert it to set|list and more.
     *
     * @param file File to search.
     * @param path Path to search.
     * @param value This will be used as default value
     *              if the config does not contains the
     *              given path. This value can be null.
     * @return New instance of {@link StringReplace}
     */
    public StringReplace getList(FileManager file, final String path, List<String> value) {
        return contains(file, path) ? getList(file, path) : new StringReplace(value);
    }

    /**
     * Get String or String List from configuration file
     * the final value will depends if the
     * given path holds a String or String list.
     *
     * Using {@link StringReplace} you can replace
     * the current String for placeholders or
     * convert it to set|list and more.
     *
     * @param path Path to search.
     * @return New instance of {@link StringReplace}
     */
    public StringReplace getStringOrList(final String path) {
        FileManager file = Objects.requireNonNull(getDefaultFile(), DEFAULT_FILE_IS_NULL_ERROR);
        return file.get().isString(path) ? getString(path) : getList(path);
    }

    /**
     * Get String or String List from configuration file
     * the final value will depends if the
     * given path holds a String or String list.
     *
     * Using {@link StringReplace} you can replace
     * the current String for placeholders or
     * convert it to set|list and more.
     *
     * @param file File to search.
     * @param path Path to search.
     * @return New instance of {@link StringReplace}
     */
    public StringReplace getStringOrList(FileManager file, final String path) {
        return file.get().isString(path) ? getString(file, path) : getList(file, path);
    }

    /**
     * Get a file section.
     *
     * @param path Path to search.
     * @return Set that contains the file section parameter.
     */
    public Set<String> getSection(final String path) {
        return Objects.requireNonNull(getDefaultFile(), DEFAULT_FILE_IS_NULL_ERROR).get().getConfigurationSection(path).getKeys(false);
    }

    /**
     * Get a file section.
     *
     * @param file File to search.
     * @param path Path to search.
     * @return Set that contains the file section parameter.
     */
    public Set<String> getSection(FileManager file, final String path) {
        return file.get().getConfigurationSection(path).getKeys(false);
    }

    /**
     * Get the default configuration file.
     * This will get the first file that has the
     * FileInfo#def field defined as true.
     *
     * Warning: This can be null if
     * we haven't a default file set.
     *
     * @return The default configuration file.
     */
    @Nullable
    public FileManager getDefaultFile() {
        return this.loadedFiles.stream().filter(FileManager::isDefault).findFirst().orElse(null);
    }

    /**
     * Get an specific configuration file.
     * This can be null if this file
     * wasn't loaded yet.
     *
     * @param fileName File name to be searched.
     * @return FileManager instance.
     */
    public static FileManager getFile(final String fileName) {
        return get().loadedFiles.stream().filter(file -> StringUtils.equalsIgnoreCase(file.getFileName(), fileName)).findFirst().orElse(null);
    }

    /**
     * Get Bukkit file configuration from the
     * given fileName.
     *
     * @param fileName File name to be searched.
     * @return Bukkit FileConfiguration.
     */
    public FileConfiguration getFileConfiguration(final String fileName) {
        return getFile(fileName).get();
    }

    public static FileUtil get() {
        return instance;
    }

    public final class FileManager {
        private FileInfo fileInfo;
        private String path;
        private File file;
        private FileConfiguration configuration;

        public FileManager(FileInfo fileInfo) {
            this(fileInfo, DEFAULT_FILE_PATH);
        }

        public FileManager(FileInfo fileInfo, final String path) {
            this.fileInfo = Objects.requireNonNull(fileInfo, "File information cannot be null.");
            this.path = path;
            this.file = new File(plugin.getDataFolder(), fileInfo.name + fileInfo.extension);

            final String name = fileInfo.name + fileInfo.extension;

            if (!file.exists()) {
                logger.info("Creating '" + fileInfo.name + "' file...");

                InputStream is = null;

                try {
                    if (!(file.getParentFile().mkdirs() || file.createNewFile())) {
                        logger.warning("An error has occurred while trying to create '" + name + "' file." +
                                '\n' +
                                "Please, Make sure the files are on the right path and try it again..."
                        );
                        return;
                    } else {
                        is = plugin.getResource(path + name);

                        if (is != null) {
                            copyFile(is, file);
                        }
                    }
                } catch (IOException ex) {
                    logger.log(Level.WARNING, "Could not create '" + name + "'.", ex);
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException ex) {
                        logger.log(Level.WARNING, "An error has occurred while closing InputStream!", ex);
                    }
                }
            }

            // Finally, load the configuration file.
            logger.info("Loading '" + fileInfo.name + "' file...");

            BufferedReader reader = null;

            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), DEFAULT_CHARSET));
                this.configuration = YamlConfiguration.loadConfiguration(reader);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ex) {
                    logger.log(Level.WARNING, "An error has occurred while closing BufferedReader!", ex);
                }
            }
        }

        /**
         * File name.
         *
         * @return File name.
         */
        public String getFileName() {
            return fileInfo.name;
        }

        /**
         * File extension.
         *
         * @return File extension.
         */
        public String getFileExtension() {
            return fileInfo.extension;
        }

        /**
         * Whenever this file is a default file.
         *
         * @return If this is the default file.
         */
        public Boolean isDefault() {
            return fileInfo.def;
        }

        /**
         * The file path.
         *
         * @return File path.
         */
        public String getPath() {
            return path;
        }

        /**
         * Get configuration file.
         *
         * @return File.
         */
        public File getFile() {
            return file;
        }

        /**
         * Get Bukkit File configuration.
         *
         * @return Configuration.
         */
        public FileConfiguration get() {
            return configuration;
        }

        /**
         * Save configuration file.
         *
         * @return If file has been saved.
         */
        public boolean save() {
            try {
                configuration.save(file);
                return true;
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Could not save " + file.getName() + "!", ex);
            }
            return false;
        }

        /**
         * Reload configuration file.
         *
         * @return If file has been reloaded.
         */
        public boolean reload() {
            try {
                configuration = YamlConfiguration.loadConfiguration(file);
                return true;
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Could not reload " + file.getName() + "!", ex);
            }
            return false;
        }
    }

    public static final class FileInfo {
        private String name;
        private String extension;
        private Boolean def = false;

        public FileInfo(char value) {
            String name = null;

            if (value == 1) {
                name = "config";
                this.def = true;
            } else if (value == 2) {
                name = "mysql";
            } else {
                throw new IllegalArgumentException("Not valid value.");
            }

            this.name = name;
            this.extension = DEFAULT_FILE_EXTENSION;
        }

        public FileInfo(String name, Boolean def) {
            this(name, DEFAULT_FILE_EXTENSION, def);
        }

        public FileInfo(String name, String extension, Boolean def) {
            this.name = name;
            this.extension = extension;
            this.def = def;
        }
    }

    /**
     * This subclass is used to replace|manage
     * texts from configuration files.
     */
    public static class StringReplace {
        private String[] text;

        StringReplace(String... text) {
            this.text = text == null || text.length <= 0 ? new String[0] : convertColor(text);
        }

        StringReplace(List<String> text) {
            this.text = text == null || text.size() <= 0 ? new String[0] : convertColor(text.toArray(new String[0]));
        }

        /**
         * Replace the alternate color codes
         * from the given string list.
         *
         * @param text Text to be converted.
         * @return Colored String.
         */
        private String[] convertColor(String... text) {
            if (text.length <= 0) return text;

            String[] ret = new String[text.length];

            for (int i = 0; i < text.length; i++) {
                ret[i] = ChatColor.translateAlternateColorCodes('&', text[i]);
            }

            return ret;
        }

        public String[] getRaw() {
            return text;
        }

        public String asString() {
            return StringUtils.join(text, ' ');
        }

        public List<String> asList() {
            return Arrays.asList(text);
        }

        public LinkedList<String> asLinkedList() {
            return new LinkedList<>(asList());
        }

        public Set<String> asSet() {
            return new HashSet<>(asList());
        }

        public StringReplace replace(String placeholder, String replacement) {
            return replace(new String[] {
                    placeholder
            }, new String[] {
                    replacement
            });
        }

        public StringReplace replace(String[] placeholders, String[] replacements) {
            if (text.length <= 0) return this;

            if (placeholders != null && replacements != null && placeholders.length == replacements.length) {
                String[] ret = new String[text.length];

                for (int i = 0; i < text.length; i++) {
                    ret[i] = StringUtils.replaceEach(text[i], placeholders, replacements);
                }

                this.text = ret;
            }

            return this;
        }
    }
}