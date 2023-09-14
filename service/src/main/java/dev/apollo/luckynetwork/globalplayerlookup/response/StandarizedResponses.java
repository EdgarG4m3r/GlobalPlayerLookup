package dev.apollo.luckynetwork.globalplayerlookup.response;

import io.javalin.http.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class StandarizedResponses {



    public static Context authenticationRequired(Context context)
    {
        JSONObject error = new JSONObject();
        JSONObject status = new JSONObject();
        status.put("friendlyMessage", "You are not authenticated! Please authenticate to continue!");
        status.put("message", "AUTHENTICATION_REQUIRED");
        status.put("code", 401);
        error.put("status", status);
        context.status(401);
        context.json(error);
        return context;
    }


    public static Context authorizationFailure(Context context)
    {
        JSONObject error = new JSONObject();
        JSONObject status = new JSONObject();
        status.put("friendlyMessage", "You are not authorized to perform this action!");
        status.put("message", "AUTHORIZATION_FAILURE");
        status.put("code", 403);
        error.put("status", status);
        context.status(403);
        context.json(error);
        return context;
    }


    public static Context methodNotAllowed(Context context, String customMessage)
    {
        JSONObject error = new JSONObject();
        JSONObject status = new JSONObject();
        status.put("friendlyMessage", customMessage);
        status.put("message", "METHOD_NOT_ALLOWED");
        status.put("code", 405);
        error.put("status", status);
        context.status(405);
        context.json(error);
        return context;
    }


    public static Context invalidParameter(Context context)
    {
        JSONObject error = new JSONObject();
        JSONObject status = new JSONObject();
        status.put("friendlyMessage", "The requested parameter is invalid!");
        status.put("message", "INVALID_PARAMETER");
        status.put("code", 400);
        JSONArray parameters = new JSONArray();
        List<ErrorContainer> errors = context.attribute("errors");
        for (ErrorContainer errorContainer : errors)
        {
            parameters.add(errorContainer.toJson());
        }
        error.put("parameters", parameters);
        error.put("status", status);
        context.status(400);
        context.json(error);
        return context;
    }


    public static Context success(Context context, String message, String customMessage, String[] names, JSONObject[] jsonObjects)
    {
        JSONObject success = new JSONObject();
        JSONObject status = new JSONObject();
        status.put("friendlyMessage", customMessage);
        status.put("message", message);
        status.put("code", 200);
        success.put("status", status);
        for (int i = 0; i < names.length; i++)
        {
            success.put(names[i], jsonObjects[i]);
        }
        context.status(200);
        context.json(success);
        return context;
    }

    public static Context success(Context context, String message, String customMessage, String name, JSONArray jsonArray)
    {
        JSONObject success = new JSONObject();
        JSONObject status = new JSONObject();
        success.put(name, jsonArray);
        status.put("friendlyMessage", customMessage);
        status.put("message", message);
        status.put("code", 200);
        success.put("status", status);
        context.status(200);
        context.json(success);
        return context;
    }

    public static Context success(Context context, String message, String customMessage, String name, JSONObject jsonObject)
    {
        JSONObject success = new JSONObject();
        JSONObject status = new JSONObject();
        status.put("friendlyMessage", customMessage);
        status.put("message", message);
        status.put("code", 200);
        success.put(name, jsonObject);
        success.put("status", status);
        context.status(200);
        context.json(success);
        return context;
    }

    public static Context success(Context context, InputStream inputStream, String applicationType)
    {
        context.status(200);
        context.contentType(applicationType);
        try
        {
            context.header("Content-Length", String.valueOf(inputStream.available()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        context.header("Content-Transfer-Encoding", "binary");
        context.header("Pragma", "no-cache");
        context.header("Expires", "0");
        context.result(inputStream);
        return context;
    }

    public static Context generalFailure(Context context, int code, String message, String friendlyMessage)
    {
        JSONObject error = new JSONObject();
        JSONObject status = new JSONObject();
        status.put("friendlyMessage", friendlyMessage);
        status.put("message", message);
        status.put("code", code);
        error.put("status", status);
        context.status(code);
        context.json(error);
        return context;
    }


}
