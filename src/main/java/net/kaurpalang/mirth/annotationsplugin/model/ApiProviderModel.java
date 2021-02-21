package net.kaurpalang.mirth.annotationsplugin.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.kaurpalang.mirth.annotationsplugin.type.ApiProviderType;

@NoArgsConstructor
@AllArgsConstructor
public class ApiProviderModel {
    @Getter private ApiProviderType type;
    @Getter private String name;

    @Override
    public String toString() {
        return String.format("%s, %s", type, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ApiProviderModel)) {
            return false;
        }

        ApiProviderModel providerModel = (ApiProviderModel) obj;

        return providerModel.getName().equals(this.name) && providerModel.getType().compareTo(this.type) == 0;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + type.hashCode();
    }
}


