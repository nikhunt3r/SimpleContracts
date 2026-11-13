package com.simplecontracts.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.saveddata.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Server-owned contract state. Each player's generated list is stored separately,
 * while all players use the same overworld day as the refresh boundary.
 */
public final class ContractData extends SavedData {
    private static final String DATA_NAME = "simplecontracts_contracts";

    private static final SavedData.Factory<ContractData> FACTORY =
            new SavedData.Factory<>(ContractData::new, ContractData::load, DataFixTypes.LEVEL);

    private static final List<ContractTemplate> TEMPLATES = List.of(
            new ContractTemplate("zombie_hunter", "Охотник на зомби",
                    "Убейте зомби", 8, 100),
            new ContractTemplate("skeleton_hunter", "Охотник на скелетов",
                    "Убейте скелетов", 6, 120),
            new ContractTemplate("iron_miner", "Добытчик железа",
                    "Добудьте железную руду", 16, 110),
            new ContractTemplate("wheat_farmer", "Фермер",
                    "Соберите пшеницу", 24, 90),
            new ContractTemplate("fisher", "Рыбак",
                    "Поймайте рыбу", 5, 130),
            new ContractTemplate("explorer", "Исследователь",
                    "Посетите новые места", 4, 150)
    );

    private final Map<UUID, PlayerContracts> players = new HashMap<>();
    private long globalRefreshDay = Long.MIN_VALUE;

    public static ContractData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    /**
     * Advances the shared refresh cycle and generates a different list for
     * every online player. Offline players are refreshed when they next join.
     */
    public void refreshOnlinePlayers(long overworldDay, Collection<ServerPlayer> onlinePlayers) {
        if (overworldDay > this.globalRefreshDay) {
            this.globalRefreshDay = overworldDay;
            setDirty();
        }

        for (ServerPlayer player : onlinePlayers) {
            ensurePlayer(player.getUUID(), this.globalRefreshDay);
        }
    }

    public List<ContractEntry> getContracts(UUID playerId) {
        PlayerContracts data = players.get(playerId);
        return data == null ? List.of() : List.copyOf(data.contracts);
    }

    private void ensurePlayer(UUID playerId, long refreshDay) {
        PlayerContracts current = players.get(playerId);
        if (current != null && current.refreshDay >= refreshDay) {
            return;
        }

        List<ContractTemplate> choices = new ArrayList<>(TEMPLATES);
        long seed = playerId.getMostSignificantBits()
                ^ playerId.getLeastSignificantBits()
                ^ (refreshDay * 0x9E3779B97F4A7C15L);
        Collections.shuffle(choices, new Random(seed));

        List<ContractEntry> generated = new ArrayList<>();
        for (int i = 0; i < Math.min(3, choices.size()); i++) {
            ContractTemplate template = choices.get(i);
            generated.add(new ContractEntry(
                    template.id(),
                    template.title(),
                    template.description(),
                    template.target(),
                    template.reward()
            ));
        }

        players.put(playerId, new PlayerContracts(refreshDay, generated));
        setDirty();
    }

    private ContractData() {
    }

    private static ContractData load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        ContractData data = new ContractData();
        data.globalRefreshDay = tag.getLong("GlobalRefreshDay");

        CompoundTag playersTag = tag.getCompound("Players");
        for (String key : playersTag.getAllKeys()) {
            try {
                UUID playerId = UUID.fromString(key);
                CompoundTag playerTag = playersTag.getCompound(key);
                long refreshDay = playerTag.getLong("RefreshDay");
                ListTag contractList = playerTag.getList("Contracts", Tag.TAG_COMPOUND);
                List<ContractEntry> contracts = new ArrayList<>();

                for (int i = 0; i < contractList.size(); i++) {
                    CompoundTag contractTag = contractList.getCompound(i);
                    contracts.add(new ContractEntry(
                            contractTag.getString("Id"),
                            contractTag.getString("Title"),
                            contractTag.getString("Description"),
                            contractTag.getInt("Target"),
                            contractTag.getLong("Reward"),
                            contractTag.getInt("Progress"),
                            contractTag.getBoolean("Claimed")
                    ));
                }

                data.players.put(playerId, new PlayerContracts(refreshDay, contracts));
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed player UUID entries rather than failing world loading.
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        tag.putLong("GlobalRefreshDay", globalRefreshDay);
        CompoundTag playersTag = new CompoundTag();

        for (Map.Entry<UUID, PlayerContracts> playerEntry : players.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putLong("RefreshDay", playerEntry.getValue().refreshDay);
            ListTag contractList = new ListTag();

            for (ContractEntry contract : playerEntry.getValue().contracts) {
                CompoundTag contractTag = new CompoundTag();
                contractTag.putString("Id", contract.id());
                contractTag.putString("Title", contract.title());
                contractTag.putString("Description", contract.description());
                contractTag.putInt("Target", contract.target());
                contractTag.putLong("Reward", contract.reward());
                contractTag.putInt("Progress", contract.progress());
                contractTag.putBoolean("Claimed", contract.claimed());
                contractList.add(contractTag);
            }

            playerTag.put("Contracts", contractList);
            playersTag.put(playerEntry.getKey().toString(), playerTag);
        }

        tag.put("Players", playersTag);
        return tag;
    }

    private record ContractTemplate(
            String id,
            String title,
            String description,
            int target,
            long reward
    ) {
    }

    private record PlayerContracts(long refreshDay, List<ContractEntry> contracts) {
    }

    public record ContractEntry(
            String id,
            String title,
            String description,
            int target,
            long reward,
            int progress,
            boolean claimed
    ) {
        public ContractEntry(String id, String title, String description, int target, long reward) {
            this(id, title, description, target, reward, 0, false);
        }
    }
}
