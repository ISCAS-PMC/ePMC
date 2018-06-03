package epmc.jani.interaction.commandline;

import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.error.UtilError;
import epmc.options.ProblemsOptions;
import epmc.util.UtilJSON;

public final class CommandLineOptions {
    private final static String TOOL_NAME = "tool-name";
    private final static String TOOL_DESCRIPTION = "tool-description";
    private final static String TOOL_REVISION_PATTERN = "tool-revision";
    private final static String USAGE_PATTERN = "usage";
    private final static String TOOL_COMMAND_PATTER = "tool-cmd";
    private final static String AVAILABLE_COMMANDS_PATTERN = "available-commands";
    private final static String AVAILABLE_PROGRAM_OPTIONS_PATTERN = "available-program-options";
    private final static String TYPE_PATTERN = "type";
    private final static String DEFAULT_PATTERN = "default";
    /** Empty string. */
    private final static String EMPTY = "";
    /** Prefix used for the options. */
    private final static String OPTION_PREFIX = "--";
    /** Identifier of the options in which plugin file names will be stored. */
    public final static String PLUGIN = "plugin";
    public final static String PLUGIN_LIST_FILE = "plugin-list-file";
    private final static String RESOURCE_BUNDLE = "CommandLineOptions";
    private final static String CAT_IDENTIFIER = "id";
    private final static String CAT_DESCRIPTION = "name";
    private final static String CAT_PARENT = "parent";
    
    private final String toolName;
    private final String toolDescription;
    private final String toolRevisionPattern;
    private final String usagePattern;
    private final String toolCommandPattern;
    private final String availableCommandsPattern;
    private final String availableProgramOptionsPattern;
    private final String typePattern;
    private final String defaultPattern;
    
    /** Available options in terms of map of option identifier to option. */
    private final Map<String,CommandLineOption> options;
    private final Map<String,CommandLineOption> optionsExternal;
    /** Available categories in terms of map of option identifier to category. */
    private final Map<String,CommandLineCategory> categories;
    /** Available options in terms of map of option identifier to option for external usage. */
    private final Map<String,CommandLineCategory> categoriesExternal;
    /** The available commands for this option set. */
    private final Map<String,CommandLineCommand> commands = new LinkedHashMap<>();
    /** Write-protected available commands for external usage. */
    private final Map<String,CommandLineCommand> commandsExternal = Collections.unmodifiableMap(commands);

    private boolean ignoreUnknown;
    private String command;

    public CommandLineOptions(JsonValue parameters, JsonValue preciseCategories) {
        assert parameters != null;
        Locale locale = Locale.getDefault();
        ResourceBundle poMsg = ResourceBundle.getBundle(RESOURCE_BUNDLE, locale);
        toolName = poMsg.getString(TOOL_NAME);
        toolDescription = poMsg.getString(TOOL_DESCRIPTION);
        toolRevisionPattern = poMsg.getString(TOOL_REVISION_PATTERN);
        usagePattern = poMsg.getString(USAGE_PATTERN);
        toolCommandPattern = poMsg.getString(TOOL_COMMAND_PATTER);
        availableCommandsPattern = poMsg.getString(AVAILABLE_COMMANDS_PATTERN);
        availableProgramOptionsPattern = poMsg.getString(AVAILABLE_PROGRAM_OPTIONS_PATTERN);
        typePattern = poMsg.getString(TYPE_PATTERN);
        defaultPattern = poMsg.getString(DEFAULT_PATTERN);
        Map<String,CommandLineCategory> categories = new LinkedHashMap<>();
        categories.putAll(parsePreciseCategories(preciseCategories));
        options = parseOptions(parameters, categories);
        optionsExternal = Collections.unmodifiableMap(options);
        this.categories = categories;
        this.categoriesExternal = Collections.unmodifiableMap(this.categories);
    }

    private static Map<String,CommandLineCategory> parsePreciseCategories(JsonValue preciseCategories) {
        if (preciseCategories == null) {
            return Collections.emptyMap();
        }
        JsonArray categoriesArray = UtilJSON.toArrayObject(preciseCategories);
        Map<String,Set<String>> childrenMap = new LinkedHashMap<>();
        Map<String,JsonObject> categoriesJsonMap = new LinkedHashMap<>();
        for (JsonValue entry : categoriesArray) {
            JsonObject jsonObject = UtilJSON.toObject(entry);
            String id = UtilJSON.getString(jsonObject, CAT_IDENTIFIER);
            String parent = UtilJSON.getStringOrNull(jsonObject, CAT_PARENT);
            Set<String> children = childrenMap.get(parent);
            if (children == null) {
                children = new HashSet<>();
                childrenMap.put(parent, children);
            }
            children.add(id);
            categoriesJsonMap.put(id, jsonObject);
        }
        Deque<String> todo = new ArrayDeque<>();
        todo.addAll(childrenMap.get(null));
        Map<String,CommandLineCategory> categoryMap = new LinkedHashMap<>();
        while (!todo.isEmpty()) {
            String id = todo.removeFirst();
            JsonObject jsonObject = categoriesJsonMap.get(id);
            String description = UtilJSON.getString(jsonObject, CAT_DESCRIPTION);
            String parentString = UtilJSON.getStringOrNull(jsonObject, CAT_PARENT);
            CommandLineCategory parent = categoryMap.get(parentString);
            CommandLineCategory category = new CommandLineCategory(id, description, parent);
            categoryMap.put(id, category);
            Set<String> children = childrenMap.get(id);
            if (children != null) {
                todo.addAll(children);
            }
        }
        return categoryMap;
    }

    private static Map<String,CommandLineOption> parseOptions(JsonValue parameters, Map<String, CommandLineCategory> categories) {
        assert parameters != null;
        assert categories != null;
        JsonArray jsonArray = UtilJSON.toArray(parameters);
        Map<String,CommandLineOption> options = new LinkedHashMap<>();
        for (JsonValue entry : jsonArray) {
            CommandLineOption option = new CommandLineOption.Builder()
                    .setJSON(UtilJSON.toObject(entry))
                    .setCategories(categories)
                    .build();
            options.put(option.getIdentifier(), option);
        }
        return options;
    }

    public void setIgnoreUnknown(boolean ignoreUnknown) {
        this.ignoreUnknown = ignoreUnknown;
    }
    
    public void parse(String[] args) {
        assert args != null;
        for (String arg : args) {
            assert arg != null;
        }
        if (args.length == 0) {
            return;
        }
        String commandName = args[0];
        UtilError.ensure(ignoreUnknown || commands.containsKey(commandName),
                ProblemsOptions.OPTIONS_COMMAND_NOT_VALID, args[0]);
        this.command = commandName;

        int argNr = 1;
        String option = null;
        String lastOption = null;
        String currentOption = null;
        while (argNr < args.length) {
            option = null;
            String arg = args[argNr];
            UtilError.ensure(!arg.equals(OPTION_PREFIX), ProblemsOptions.OPTIONS_PROGRAM_OPTION_NOT_VALID, arg);
            if (arg.length() >= 2 && arg.substring(0, 2).equals(OPTION_PREFIX)) {
                option = arg.substring(2, arg.length());
            }
            UtilError.ensure(ignoreUnknown || option == null
                    || options.containsKey(option),
                    ProblemsOptions.OPTIONS_PROGRAM_OPTION_NOT_VALID, option);
            UtilError.ensure(lastOption == null || option == null,
                    ProblemsOptions.OPTIONS_NO_VALUE_FOR_OPTION, lastOption);
            UtilError.ensure(currentOption != null || option != null,
                    ProblemsOptions.OPTIONS_NO_OPTION_FOR_VALUE, arg);
            if (option == null) {
                if (currentOption.equals(PLUGIN) || currentOption.equals(PLUGIN_LIST_FILE) || !ignoreUnknown) {
                    String value = arg;
                    try {
                        parse(currentOption, value);
                    } catch (EPMCException e) {
                        String message = e.getProblem().getMessage(getLocale());
                        MessageFormat formatter = new MessageFormat(EMPTY);
                        formatter.applyPattern(ProblemsOptions.OPTIONS_PARSE_OPTION_FAILED.getMessage(getLocale()));
                        String formattedMessage = formatter.format(new Object[]{currentOption});
                        System.err.println(formattedMessage);
                        formatter.applyPattern(message);
                        formattedMessage = formatter.format(e.getArguments());
                        System.err.println(formattedMessage);
                        System.exit(1);
                    }
                }
            }
            lastOption = option;
            if (option != null) {
                currentOption = option;
            }
            argNr++;
        }
        UtilError.ensure(lastOption == null,
                ProblemsOptions.OPTIONS_NO_VALUE_FOR_OPTION, lastOption);

    }
    
    JsonValue getUpdateRequest() {
        // TODO
        return null;
    }
    
    /**
     * Obtain the locale specified for this options set.
     * 
     * @return locale specified for this options set
     */
    public Locale getLocale() {
        return Locale.getDefault();
    }
    
    /**
     * Parse the option with the given identifier, thereby setting its value.
     * The option identifier and the value to parse must not be {@code null}.
     * The option with the given identifier must exist in this options set. If
     * the value could be parsed correctly, it will be set (or added to) the
     * option. Otherwise, an {@link EPMCException} will be thrown to provide
     * feedback to the user about the correct usage of the option.
     * 
     * @param option identifier of the option the value of which to parse
     * @param value value to parse
     */
    public void parse(String option, String value) {
        assert option != null;
        assert value != null;
        CommandLineOption opt = options.get(option);
        assert opt != null;
        opt.parse(value);
    }
    
    public String getCommand() {
        return command;
    }

    public String getToolName() {
        return toolName;
    }

    public String getToolDescription() {
        return toolDescription;
    }

    public Map<String,CommandLineOption> getAllOptions() {
        return optionsExternal;
    }
    
    public Map<String,CommandLineCommand> getCommands() {
        return commandsExternal;
    }
    
    public Map<String,CommandLineCategory> getAllCategories() {
        return categoriesExternal;
    }

    public String getRunningToolRevisionPatter() {
        return toolRevisionPattern;
    }

    public String getUsagePattern() {
        return usagePattern;
    }

    public String getToolCmdPattern() {
        return toolCommandPattern;
    }

    public String getAvailableCommandsPattern() {
        return availableCommandsPattern;
    }

    public String getAvailableProgramOptionsPattern() {
        return availableProgramOptionsPattern;
    }
    
    public String getTypePattern() {
        return typePattern;
    }
    
    public String getDefaultPattern() {
        return defaultPattern;
    }
}