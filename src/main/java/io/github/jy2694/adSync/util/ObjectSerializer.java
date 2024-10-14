package io.github.jy2694.adSync.util;

import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.util.Base64;

public class ObjectSerializer {
    /**
     * Serializes
     * @param object to serialize
     * @return serialized object as a base64 encoded string
     */
    public static String serializeObject(Object object) {
        try{
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream objectOutputStream = new BukkitObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.close();
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (NotSerializableException e){
            throw new IllegalArgumentException("The object must be serializable");
        } catch (IOException e) {
            throw new RuntimeException("Error serializing object", e);
        }
    }

    /**
     * Deserializes
     * @param encodedString encoded object as a base64 encoded string
     * @param clazz class of the object to deserialize to
     * @return deserialized object
     */
    public static <T> T deserializeObject(String encodedString, Class<T> clazz){
        try{
            byte[] bytes = Base64.getDecoder().decode(encodedString);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            BukkitObjectInputStream objectInputStream = new BukkitObjectInputStream(byteArrayInputStream);
            return clazz.cast(objectInputStream.readObject());
        } catch (IOException | ClassNotFoundException e){
            throw new RuntimeException("Error deserializing object", e);
        }

    }
}
