package ec.edu.unl.lojavents.storage;

public record StoredFile(String id, String fileName, String contentType, long size) {
}
