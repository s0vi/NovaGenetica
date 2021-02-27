package me.orangemonkey68.novagenetica.abilities;

import me.orangemonkey68.novagenetica.NovaGenetica;
import me.orangemonkey68.novagenetica.NovaGeneticaEntityType;
import me.orangemonkey68.novagenetica.item.ItemHelper;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegistrationHelper {
    private static final Registry<Ability> ABILITY_REGISTRY = NovaGenetica.ABILITY_REGISTRY;

    /**
     * A map of each EntityTypee and the set of abilities they can drop
     */
    public static final Map<EntityType<?>, Set<Ability>> ENTITY_TYPE_ABILITY_MAP = new HashMap<>();
    /**
     * A map of each EntityType and their color
     */
    public static final Map<EntityType<?>, Integer> ENTITY_TYPE_COLOR_MAP = new HashMap<>();

    private final FabricItemGroupBuilder itemGroupBuilder;
    private final Map<Subsection, List<ItemStack>> itemMap = new HashMap<>();


    public RegistrationHelper (Identifier itemGroupId) {
        this.itemGroupBuilder = FabricItemGroupBuilder.create(itemGroupId);
        //Set up map
        itemMap.put(Subsection.START, new ArrayList<>());
        itemMap.put(Subsection.SYRINGE, new ArrayList<>());
        itemMap.put(Subsection.GENE, new ArrayList<>());
        itemMap.put(Subsection.MOB_FLAKES, new ArrayList<>());
        itemMap.put(Subsection.END, new ArrayList<>());
    }

    /**
     * Registers all the genes, abilities, syringes, and other items for you.
     * @param ability the {@link Ability} to register.
     * @param abilityId the {@link Identifier} of the ability.
     * @param entityTypeColorMap a map of all the entityTypes that can drop this ability, and the colors their {@code Mob Flakes} should be
     */
    public void register(Ability ability, Identifier abilityId, Map<EntityType<?>, Integer> entityTypeColorMap){
        if(!AbilityValidator.validate(ability)) {
            NovaGenetica.LOGGER.error("Ability: \"{}\" has failed a check. Submit a bug report to the mod authors!", abilityId);
            return;
        }
        //Register ability to ABILITY_REGISTRY
        Registry.register(ABILITY_REGISTRY, abilityId, ability);

        //Create and put syringe in itemMap
        ItemStack syringe = ItemHelper.stackWithAbility(abilityId, NovaGenetica.FILLED_SYRINGE_ITEM);
        addItemToGroup(Subsection.SYRINGE, syringe);

        //Genes need all of the Abilities to be registered, so they're registered in the build function
        //Loops over given EntityTypes and registers their colors and maps their colors, and registers a Mob Flake
        entityTypeColorMap.forEach((type, mobColor) -> {
            if(!ENTITY_TYPE_ABILITY_MAP.containsKey(type))
                ENTITY_TYPE_ABILITY_MAP.put(type, new HashSet<>());
            ENTITY_TYPE_ABILITY_MAP.get(type).add(ability);

            if(!ENTITY_TYPE_COLOR_MAP.containsKey(type)){
                ENTITY_TYPE_COLOR_MAP.put(type, mobColor);
                addItemToGroup(Subsection.MOB_FLAKES, ItemHelper.stackWithEntityType(type, NovaGenetica.MOB_FLAKES));
            }
        });


        addItemToGroup(Subsection.GENE, ItemHelper.stackWithAbility(abilityId, NovaGenetica.GENE_ITEM));

        //Adds to Ability.ABILITY_ENTITY_MAP
        Ability.ABILITY_ENTITY_MAP.put(ability, entityTypeColorMap.keySet());

        //Register abilities to entity
        entityTypeColorMap.forEach((type, entityColor) -> ((NovaGeneticaEntityType)type).registerAbility(ability));
    }

    /**
     * Adds an {@link ItemStack} to the {@link ItemGroup} returned in {@link #buildGroup(ItemStack)}
     * <b>NOTE:</b> the order in which you add items here will be the same order they appear in the {@link ItemGroup}
     * @param stack the {@link ItemStack} to add
     */
    public void addItemToGroup(Subsection section, ItemStack stack){
        itemMap.get(section).add(stack);
    }

    /**
     *
     * @param groupIcon the {@link ItemStack} to show on the tab in the creative inventory
     * @return the completed ItemGroup
     */
    public ItemGroup buildGroup(ItemStack groupIcon){


        List<ItemStack> totalList = Stream.of(
                itemMap.get(Subsection.START),
                itemMap.get(Subsection.SYRINGE),
                itemMap.get(Subsection.GENE),
                itemMap.get(Subsection.MOB_FLAKES),
                itemMap.get(Subsection.END)
        ).flatMap(Collection::stream).collect(Collectors.toList());

        return itemGroupBuilder.icon(() -> groupIcon)
                .appendItems(list -> list.addAll(totalList))
                .build();
    }

    public enum Subsection {
        START,
        SYRINGE,
        GENE,
        MOB_FLAKES,
        END
    }
}
