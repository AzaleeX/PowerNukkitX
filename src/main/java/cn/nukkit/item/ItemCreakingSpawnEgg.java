package cn.nukkit.item;

public class ItemCreakingSpawnEgg extends ItemSpawnEgg {
    public ItemCreakingSpawnEgg() {
        super(CREAKING_SPAWN_EGG);
    }

    @Override
    public int getEntityNetworkId() {
        return 145;
    }

    @Override
    public void setDamage(int meta) {

    }
}