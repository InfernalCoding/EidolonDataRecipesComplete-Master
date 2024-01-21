package dev.infernal_coding.eidolonrecipes.spells;

import elucent.eidolon.capability.IReputation;
import elucent.eidolon.spell.AltarInfo;
import elucent.eidolon.tile.EffigyTileEntity;
import elucent.eidolon.tile.GobletTileEntity;

import java.util.Optional;

public class SpellInfo {

    public final Optional<EffigyTileEntity> effigy;
    public final Optional<GobletTileEntity> goblet;
    public final Optional<AltarInfo> altar;
    public final IReputation reputation;

    public SpellInfo(Optional<EffigyTileEntity> effigy, Optional<GobletTileEntity> goblet, Optional<AltarInfo> altar, IReputation reputation) {
        this.effigy = effigy;
        this.goblet = goblet;
        this.altar = altar;
        this.reputation = reputation;
    }
}
