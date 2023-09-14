package dev.apollo.luckynetwork.globalplayerlookup.response;

import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class InputFilter {

    static Pattern minecraftUsernamePattern = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");

    public static ErrorContainer validateNickname(String parm, ParamField field, Context context) {
        String input = getParameterValue(parm, field, context);
        ErrorContainer errorContainer = validateEmptyOrNull(parm, field, context);
        if (errorContainer != null) {
            return errorContainer;
        }

        if (input.length() > 16) {
            return buildAndAddError(parm, "Not a valid nickname or UUID", context);
        }

        if (input.length() < 3) {
            return buildAndAddError(parm, "Not a valid nickname or UUID", context);
        }


        if (!minecraftUsernamePattern.matcher(input).matches()) {
            return buildAndAddError(parm, "Not a valid nickname or UUID", context);
        }

        return null;
    }
    private static ErrorContainer validateEmptyOrNull(String param, ParamField field, Context context) {
        String value = getParameterValue(param, field, context);
        String message = param.substring(0, 1).toUpperCase() + param.substring(1) + " tidak boleh kosong";
        if (value == null) {
            return buildAndAddError(param, message, context);
        }
        if (value.isEmpty()) {
            return buildAndAddError(param, message, context);
        }
        return null;
    }

    private static String getParameterValue(String param, ParamField field, Context context) {
        String value = null;
        if (field.equals(ParamField.FORM)) {
            value = context.formParam(param);
        } else if (field.equals(ParamField.QUERY)) {
            value = context.queryParam(param);
        } else if (field.equals(ParamField.PATH)) {
            value = context.pathParam(param);
        } else if (field.equals(ParamField.HEADER)) {
            value = context.header(param);
        }
        return value;
    }

    private static ErrorContainer buildAndAddError(String field, String message, Context context) {
        ErrorContainer errorContainer = new ErrorContainer(field, message);
        addErrorContainerToContext(context, errorContainer);
        return errorContainer;
    }


    private static void addErrorContainerToContext(Context context, ErrorContainer errorContainer)
    {
        if (context.attribute("hasErrors") == null)
        {
            context.attribute("errors", null);
        }
        if (context.attribute("errors") == null)
        {
            context.attribute("errors", new ArrayList<ErrorContainer>());
            context.attribute("hasErrors", true);
        }
        ArrayList<ErrorContainer> errors = context.attribute("errors");
        errors.add(errorContainer);
    }

}
