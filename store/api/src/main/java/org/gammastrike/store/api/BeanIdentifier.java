package org.gammastrike.store.api;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.UUID;

public class BeanIdentifier implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final BeanIdentifier UNDEFINED = new BeanIdentifier(null, new Annotation[0]);

    public static BeanIdentifier buildFrom(String identifier, Annotation... qualifiers) {
        if (identifier != null && !identifier.isEmpty()) {
            UUID uuid = UUID.fromString(identifier);
            return new BeanIdentifier(uuid, qualifiers);
        } else {
            return UNDEFINED;
        }
    }

    public static BeanIdentifier buildFrom(String identifier, Collection<Annotation> qualifiers) {
        Annotation[] qualifierArray = qualifiers.toArray(new Annotation[qualifiers.size()]);
        return buildFrom(identifier, qualifierArray);
    }

    private final UUID uuid;

    private final Annotation[] qualifiers;

    public BeanIdentifier(Annotation... qualifiers) {
        this(UUID.randomUUID(), qualifiers);
    }

    private BeanIdentifier(UUID uuid, Annotation... qualifiers) {
        this.uuid = uuid;
        this.qualifiers = requireNonNull(qualifiers);
    }

    public Annotation[] getQualifiers() {
        return qualifiers;
    }

    public boolean isUndefined() {
        return uuid == null;
    }

    public String asString() {
        return String.valueOf(uuid);
    }

    @Override
    public String toString() {
        return "BeanIdentifier [" + uuid + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BeanIdentifier other = (BeanIdentifier) obj;
        if (uuid == null) {
            if (other.uuid != null) {
                return false;
            }
        } else if (!uuid.equals(other.uuid)) {
            return false;
        }
        return true;
    }
}
