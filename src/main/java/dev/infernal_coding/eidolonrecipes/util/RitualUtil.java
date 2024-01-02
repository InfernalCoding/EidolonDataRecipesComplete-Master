package dev.infernal_coding.eidolonrecipes.util;

import com.ibm.icu.impl.Pair;
import dev.infernal_coding.eidolonrecipes.registry.EidolonReflectedRegistries;
import elucent.eidolon.codex.RitualPage;
import elucent.eidolon.ritual.IRequirement;
import elucent.eidolon.ritual.ItemRequirement;
import elucent.eidolon.ritual.MultiItemSacrifice;
import elucent.eidolon.ritual.Ritual;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;

import java.util.ArrayList;
import java.util.List;

public class RitualUtil {

    static ItemStack getSacrificeItem(Object sacrifice) {
        if (sacrifice instanceof ItemStack) {
            return (ItemStack) sacrifice;
        } else if (sacrifice instanceof ITag) {
            ITag<Item> tag = (ITag<Item>) sacrifice;
            return new ItemStack(tag.getAllElements().get(0));
        } else if (sacrifice instanceof Item) {
            return new ItemStack((Item) sacrifice);
        } else if (sacrifice instanceof Block) {
            return new ItemStack((Block) sacrifice);
        }
        return ItemStack.EMPTY;
    }

    public static Pair<ItemStack, RitualPage.RitualIngredient[]> getRitualInputs(Ritual ritual) {
        List<RitualPage.RitualIngredient> inputs = new ArrayList<>();
        List<IRequirement> requirements = new ArrayList<>(ritual.getRequirements());
        Object sacrifice = EidolonReflectedRegistries.sacrificeMap.get(ritual);
        ItemStack pedestal;

        if (sacrifice instanceof MultiItemSacrifice) {
            MultiItemSacrifice multi = (MultiItemSacrifice) sacrifice;
            pedestal = getSacrificeItem(multi.main);

            multi.items.forEach(o -> {
                if (o instanceof ItemStack) {
                    ItemStack item1 = (ItemStack) o;

                    for (int i = 0; i < requirements.size(); i++) {
                        IRequirement rq = requirements.get(i);
                        if (rq instanceof ItemRequirement) {
                            ItemRequirement itemRequirement = (ItemRequirement) rq;
                            if (itemRequirement.getMatch() instanceof ItemStack) {
                                ItemStack item2 = (ItemStack) itemRequirement.getMatch();
                                if (ItemStack.areItemStacksEqual(item1, item2)) {
                                    requirements.remove(i);
                                    break;
                                }
                            }
                        }
                    }
                    inputs.add(new RitualPage.RitualIngredient(item1, true));
                } else if (o instanceof ITag) {
                    ITag<Item> tag1 = (ITag<Item>) o;

                    for (int i = 0; i < requirements.size(); i++) {
                        IRequirement rq = requirements.get(i);

                        if (rq instanceof ItemRequirement) {
                            ItemRequirement itemRequirement = (ItemRequirement) rq;
                            if (itemRequirement.getMatch() instanceof ITag) {
                                ITag<Item> tag2 = (ITag<Item>) itemRequirement.getMatch();
                                if (tag1 == tag2) {
                                    requirements.remove(i);
                                    break;
                                }
                            }
                        }
                    }

                    for (int i = 0; i < requirements.size() ; i++) {
                        IRequirement rq = requirements.get(i);

                        if (rq instanceof ItemRequirement) {
                            ItemRequirement itemRequirement = (ItemRequirement) rq;
                            if (itemRequirement.getMatch() instanceof ITag) {
                                ITag<Item> tag2 = (ITag<Item>) itemRequirement.getMatch();
                                if (tag1 == tag2) {
                                    requirements.remove(i);
                                    break;
                                }
                            }
                        }
                    }

                    if (!tag1.getAllElements().isEmpty()) {
                        inputs.add(new RitualPage.RitualIngredient(new
                                ItemStack(tag1.getAllElements().get(0)), true));
                    }
                } else if (o instanceof Item) {
                    Item item1 = (Item) o;

                    for (int i = 0; i < requirements.size(); i++) {
                        IRequirement rq = requirements.get(i);

                        if (rq instanceof ItemRequirement) {

                            ItemRequirement itemRequirement = (ItemRequirement) rq;
                            if (itemRequirement.getMatch() instanceof Item) {
                                Item item2 = (Item) itemRequirement.getMatch();
                                if (item1 == item2) {
                                    requirements.remove(i);
                                    break;
                                }
                            }
                        }
                    }
                    inputs.add(new RitualPage.RitualIngredient(new ItemStack(item1), true));
                } else if (o instanceof Block) {
                    Block block1 = (Block) o;

                    for (int i = 0; i < requirements.size(); i++) {
                        IRequirement rq = requirements.get(i);

                        if (rq instanceof ItemRequirement) {
                            ItemRequirement itemRequirement = (ItemRequirement) rq;
                            if (itemRequirement.getMatch() instanceof Block) {
                                Block block2 = (Block) itemRequirement.getMatch();
                                if (block1 == block2) {
                                    requirements.remove(i);
                                    break;
                                }
                            }
                        }
                    }
                    inputs.add(new RitualPage.RitualIngredient(
                            new ItemStack(block1.asItem()), true));
                }
            });
        } else {
            pedestal = getSacrificeItem(sacrifice);
        }

        requirements.forEach(iRequirement -> {
            if (iRequirement instanceof ItemRequirement) {
                ItemRequirement itemRequirement = (ItemRequirement) iRequirement;
                if (itemRequirement.getMatch() instanceof Item) {
                    Item item = (Item) itemRequirement.getMatch();
                    inputs.add(new RitualPage.RitualIngredient(new ItemStack(item), false));
                } else if (itemRequirement.getMatch() instanceof Block) {
                    Block block = ((Block) itemRequirement.getMatch());
                    inputs.add(new RitualPage.RitualIngredient(new ItemStack(block), false));
                } else if (itemRequirement.getMatch() instanceof ITag) {
                    ITag<Item> tag = (ITag<Item>) itemRequirement.getMatch();
                    if (!tag.getAllElements().isEmpty()) {
                        Item item = tag.getAllElements().get(0);
                        inputs.add(new RitualPage.RitualIngredient(new ItemStack(item), false));
                    }
                } else if (itemRequirement.getMatch() instanceof ItemStack) {
                    ItemStack itemStack = (ItemStack) itemRequirement.getMatch();
                    inputs.add(new RitualPage.RitualIngredient(itemStack, false));
                }
            }
        });
        return Pair.of(pedestal, inputs.toArray(new RitualPage.RitualIngredient[0]));
    }

}
