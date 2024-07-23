package de.unistuttgart.iste.meitrex.rulesengine;

import de.unistuttgart.iste.meitrex.rulesengine.util.DataRegistry;

import java.util.*;

/**
 * Registry for {@link Rule}s.
 */
public class RuleRegistry extends DataRegistry<Rule, UUID> {

    public RuleRegistry() {
        super(new HashMap<>()); // no rules by default
    }

    @Override
    protected UUID getId(Rule data) {
        return data.getId();
    }

    public List<Rule> getRulesForEventType(String identifier) {
        return getAll().stream()
                .filter(rule -> rule.getTriggerEventTypeIdentifiers().contains(identifier))
                .toList();
    }
}
