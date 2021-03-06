package app.artyomd.injector;


import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class InjectorExtension {
    private boolean enabled = true;
    private List<String> defaultExcludeGroups = new ArrayList<>();
    private List<String> excludeGroups = new ArrayList<>();
    private List<String> excludeNames = new ArrayList<>();
    private List<String> forceExcludeNames = new ArrayList<>();
    private List<String> forceExcludeGroups = new ArrayList<>();
    private String dexLocation = "/outputs/inject/";
    private String defaultDexName = "inject.dex";
    private Map<String, List<String>> groups = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDexLocation() {
        return dexLocation;
    }

    public void setExcludeGroups(List<String> excludeGroups) {
        this.excludeGroups = excludeGroups;
    }

    public void setExcludeNames(List<String> excludeNames) {
        this.excludeNames = excludeNames;
    }

    public void setForceExcludeNames(List<String> forceExcludeNames) {
        this.forceExcludeNames = forceExcludeNames;
    }

    public void setForceExcludeGroups(List<String> forceExcludeGroups) {
        this.forceExcludeGroups = forceExcludeGroups;
    }

    public void setDexLoaction(String dexLocation) {
        this.dexLocation = dexLocation;
    }

    public void setDefaultDexName(String defaultDexName) {
        this.defaultDexName = defaultDexName;
    }

    public void setGroups(Map<String, List<String>> groups) {
        this.groups = groups;
    }

    public InjectorExtension() {
        defaultExcludeGroups.add("com.android.*");
        defaultExcludeGroups.add("android.arch.*");
    }

    public boolean checkArtifact(ResolvedArtifact artifact) {
        ModuleVersionIdentifier id = artifact.getModuleVersion().getId();
        return checkGroup(id.getGroup()) && checkName(id.getName());
    }

    public boolean checkForceExcluded(ResolvedArtifact artifact) {
        String name = artifact.getModuleVersion().getId().getName();
        for (String excludeName : forceExcludeNames) {
            if (name.matches(excludeName)) {
                return true;
            }
        }
        String group = artifact.getModuleVersion().getId().getGroup();
        for (String excludeName : forceExcludeGroups) {
            if (group.matches(excludeName)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkForceExcluded(Dependency dependency) {
        String name = dependency.getName();
        for (String excludeName : forceExcludeNames) {
            if (name.matches(excludeName)) {
                return true;
            }
        }
        String group = dependency.getGroup();
        if (group != null) {
            for (String excludeName : forceExcludeGroups) {
                if (group.matches(excludeName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkName(String name) {
        for (String excludeName : excludeNames) {
            if (name.matches(excludeName)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkGroup(String group) {
        for (String excludeGroup : defaultExcludeGroups) {
            if (group.matches(excludeGroup)) {
                return false;
            }
        }

        for (String excludeGroup : excludeGroups) {
            if (group.matches(excludeGroup)) {
                return false;
            }
        }
        return true;
    }

    public Map<String, List<ResolvedArtifact>> getDexes(List<ResolvedArtifact> artifacts) {
        Map<String, List<ResolvedArtifact>> dexMap = new HashMap<>();
        artifacts.forEach(resolvedArtifact -> {
            List<String> names = getDexName(resolvedArtifact);
            names.forEach(name -> {
                if (dexMap.containsKey(name)) {
                    dexMap.get(name).add(resolvedArtifact);
                } else {
                    List<ResolvedArtifact> list = new ArrayList<>();
                    list.add(resolvedArtifact);
                    dexMap.put(name, list);
                }
            });
        });
        return dexMap;
    }

    private List<String> getDexName(ResolvedArtifact artifact) {
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
            if (checkContains(artifact, entry.getValue())) {
                names.add(entry.getKey());
            }
        }
        if (names.isEmpty()) {
            names.add(defaultDexName);
        }
        return names;
    }

    private static boolean checkContains(ResolvedArtifact artifact, List<String> data) {
        return data.contains(artifact.getModuleVersion().getId().getGroup()) || data.contains(artifact.getModuleVersion().getId().getName());
    }
}