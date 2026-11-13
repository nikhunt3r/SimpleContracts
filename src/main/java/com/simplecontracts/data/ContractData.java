package com.simplecontracts.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import com.simpleeconomy.api.EconomyAPI;

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
                    "Посетите новые места", 4, 150),
            new ContractTemplate("spider_hunter", "Охотник на пауков",
                    "Убейте пауков", 8, 120),
            new ContractTemplate("woodcutter", "Лесоруб",
                    "Срубите брёвна", 32, 110),
            new ContractTemplate("stone_miner", "Каменщик",
                    "Добудьте камень", 32, 100)
    );

    private final Map<UUID, PlayerContracts> players = new HashMap<>();
    private final Set<String> exploredLocations = new HashSet<>();
    private long globalRefreshDay = Long.MIN_VALUE;
    private long globalRefreshVersion;

    public static ContractData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public void refreshOnlinePlayers(long overworldDay, Collection<ServerPlayer> onlinePlayers) {
        for (ServerPlayer player : onlinePlayers) {
            getOrCreateContracts(player.getUUID(), overworldDay);
        }
    }

    /**
     * Starts a new shared refresh cycle immediately.
     * Accepted, unclaimed contracts are preserved; available offers are regenerated.
     */
    public int forceRefresh(long overworldDay, Collection<ServerPlayer> onlinePlayers) {
        if (overworldDay > this.globalRefreshDay) {
            this.globalRefreshDay = overworldDay;
        }

        this.globalRefreshVersion++;
        for (ServerPlayer player : onlinePlayers) {
            ensurePlayer(player.getUUID(), this.globalRefreshDay, this.globalRefreshVersion);
        }

        setDirty();
        return onlinePlayers.size();
    }

    public List<ContractEntry> getOrCreateContracts(UUID playerId, long overworldDay) {
        if (overworldDay > this.globalRefreshDay) {
            this.globalRefreshDay = overworldDay;
            this.globalRefreshVersion++;
            setDirty();
        }

        ensurePlayer(playerId, this.globalRefreshDay, this.globalRefreshVersion);
        return getContracts(playerId);
    }

    public List<ContractEntry> getContracts(UUID playerId) {
        PlayerContracts data = players.get(playerId);
        return data == null ? List.of() : List.copyOf(data.contracts());
    }

    public List<ContractEntry> getActiveContracts(UUID playerId) {
        return getContracts(playerId).stream()
                .filter(contract -> contract.accepted() && !contract.claimed())
                .toList();
    }

    /**
     * Accepts one of the player's currently offered contracts.
     * The contract is not eligible for progress before this server-side action.
     */
    public boolean acceptContract(UUID playerId, int contractIndex) {
        PlayerContracts current = players.get(playerId);
        if (current == null || contractIndex < 0 || contractIndex >= current.contracts().size()) {
            return false;
        }

        List<ContractEntry> updated = new ArrayList<>(current.contracts());
        ContractEntry contract = updated.get(contractIndex);
        if (contract.accepted() || contract.claimed()) {
            return false;
        }

        updated.set(contractIndex, contract.withAccepted(true));
        players.put(playerId, new PlayerContracts(current.refreshDay(), current.refreshVersion(), updated));
        setDirty();
        return true;
    }

    /**
     * Advances progress only for accepted, unfinished contracts with the given id.
     */
    public boolean advanceProgress(UUID playerId, String contractId, int amount) {
        if (amount <= 0) {
            return false;
        }

        PlayerContracts current = players.get(playerId);
        if (current == null) {
            return false;
        }

        List<ContractEntry> updated = new ArrayList<>(current.contracts());
        boolean changed = false;

        for (int i = 0; i < updated.size(); i++) {
            ContractEntry contract = updated.get(i);
            if (contract.id().equals(contractId)
                    && contract.accepted()
                    && !contract.claimed()
                    && contract.progress() < contract.target()) {
                int nextProgress = Math.min(contract.target(), contract.progress() + amount);
                updated.set(i, contract.withProgress(nextProgress));
                changed = true;
                break;
            }
        }

        if (changed) {
            players.put(playerId, new PlayerContracts(current.refreshDay(), current.refreshVersion(), updated));
            setDirty();
        }

        return changed;
    }

    public boolean claimReward(ServerPlayer player, int contractIndex) {
        PlayerContracts current = players.get(player.getUUID());
        if (current == null || contractIndex < 0 || contractIndex >= current.contracts().size()) {
            return false;
        }

        ContractEntry contract = current.contracts().get(contractIndex);
        if (!contract.accepted() || contract.claimed() || contract.progress() < contract.target()) {
            return false;
        }

        // EconomyAPI stores the token alongside the balance and history update.
        EconomyAPI.addContractReward(player, contract.reward(), contract.title(), contract.rewardToken());

        List<ContractEntry> updated = new ArrayList<>(current.contracts());
        updated.set(contractIndex, contract.withClaimed(true));
        players.put(player.getUUID(), new PlayerContracts(current.refreshDay(), current.refreshVersion(), updated));
        setDirty();
        return true;
    }

    public boolean advanceForBlock(UUID playerId, String contractId, int amount) {
        return advanceProgress(playerId, contractId, amount);
    }

    public boolean recordExploration(ServerPlayer player) {
        UUID playerId = player.getUUID();
        boolean hasActiveExplorer = getActiveContracts(playerId).stream()
                .anyMatch(contract -> contract.id().equals("explorer")
                        && !contract.claimed()
                        && contract.progress() < contract.target());
        if (!hasActiveExplorer) {
            return false;
        }

        int chunkX = player.blockPosition().getX() >> 4;
        int chunkZ = player.blockPosition().getZ() >> 4;
        String locationKey = player.level().dimension().location()
                + ":" + chunkX + ":" + chunkZ;
        if (!exploredLocations.add(playerId + ":" + locationKey)) {
            return false;
        }

        setDirty();
        return advanceProgress(playerId, "explorer", 1);
    }

    private void ensurePlayer(UUID playerId, long refreshDay, long refreshVersion) {
        PlayerContracts current = players.get(playerId);
        if (current != null
                && current.refreshDay() >= refreshDay
                && current.refreshVersion() >= refreshVersion) {
            return;
        }

        // Keep accepted contracts while they are active or awaiting reward collection.
        // Claimed contracts are removed at the next shared daily refresh.
        List<ContractEntry> generated = new ArrayList<>();
        if (current != null) {
            current.contracts().stream()
                    .filter(contract -> contract.accepted() && !contract.claimed())
                    .forEach(generated::add);
        }

        Set<String> previousOffers = new HashSet<>();
        if (current != null) {
            current.contracts().stream()
                    .filter(contract -> !contract.accepted())
                    .map(ContractEntry::id)
                    .forEach(previousOffers::add);
        }

        List<ContractTemplate> choices = new ArrayList<>(TEMPLATES);
        choices.removeIf(template -> previousOffers.contains(template.id()));

        long seed = playerId.getMostSignificantBits()
                ^ playerId.getLeastSignificantBits()
                ^ (refreshDay * 0x9E3779B97F4A7C15L)
                ^ (refreshVersion * 0xD1B54A32D192ED03L);
        Collections.shuffle(choices, new Random(seed));

        for (ContractTemplate template : choices) {
            if (generated.size() >= 3) {
                break;
            }
            boolean alreadyActive = generated.stream().anyMatch(entry -> entry.id().equals(template.id()));
            if (!alreadyActive) {
                generated.add(new ContractEntry(
                        template.id(),
                        template.title(),
                        template.description(),
                        template.target(),
                        template.reward(),
                        0,
                        false,
                        false,
                        UUID.randomUUID().toString()
                ));
            }
        }

        players.put(playerId, new PlayerContracts(refreshDay, refreshVersion, generated));
        setDirty();
    }

    private ContractData() {
    }

    private static ContractData load(
            CompoundTag tag,
            net.minecraft.core.HolderLookup.Provider registries
    ) {
        ContractData data = new ContractData();
        data.globalRefreshDay = tag.getLong("GlobalRefreshDay");
        data.globalRefreshVersion = tag.getLong("GlobalRefreshVersion");

        ListTag exploredTag = tag.getList("ExploredLocations", Tag.TAG_STRING);
        for (int i = 0; i < exploredTag.size(); i++) {
            data.exploredLocations.add(exploredTag.getString(i));
        }

        CompoundTag playersTag = tag.getCompound("Players");
        for (String key : playersTag.getAllKeys()) {
            try {
                UUID playerId = UUID.fromString(key);
                CompoundTag playerTag = playersTag.getCompound(key);
                long refreshDay = playerTag.getLong("RefreshDay");
                long refreshVersion = playerTag.getLong("RefreshVersion");
                ListTag contractList = playerTag.getList("Contracts", Tag.TAG_COMPOUND);
                List<ContractEntry> contracts = new ArrayList<>();

                for (int i = 0; i < contractList.size(); i++) {
                    CompoundTag contractTag = contractList.getCompound(i);
                    String rewardToken = contractTag.getString("RewardToken");
                    if (rewardToken.isBlank()) {
                        rewardToken = UUID.randomUUID().toString();
                        contractTag.putString("RewardToken", rewardToken);
                        data.setDirty();
                    }
                    contracts.add(new ContractEntry(
                            contractTag.getString("Id"),
                            contractTag.getString("Title"),
                            contractTag.getString("Description"),
                            contractTag.getInt("Target"),
                            contractTag.getLong("Reward"),
                            contractTag.getInt("Progress"),
                            contractTag.getBoolean("Claimed"),
                            contractTag.getBoolean("Accepted"),
                            rewardToken
                    ));
                }

                data.players.put(playerId, new PlayerContracts(refreshDay, refreshVersion, contracts));
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed player UUID entries rather than failing world loading.
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        tag.putLong("GlobalRefreshDay", globalRefreshDay);
        tag.putLong("GlobalRefreshVersion", globalRefreshVersion);
        CompoundTag playersTag = new CompoundTag();

        for (Map.Entry<UUID, PlayerContracts> playerEntry : players.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putLong("RefreshDay", playerEntry.getValue().refreshDay());
            playerTag.putLong("RefreshVersion", playerEntry.getValue().refreshVersion());
            ListTag contractList = new ListTag();

            for (ContractEntry contract : playerEntry.getValue().contracts()) {
                CompoundTag contractTag = new CompoundTag();
                contractTag.putString("Id", contract.id());
                contractTag.putString("Title", contract.title());
                contractTag.putString("Description", contract.description());
                contractTag.putInt("Target", contract.target());
                contractTag.putLong("Reward", contract.reward());
                contractTag.putInt("Progress", contract.progress());
                contractTag.putBoolean("Claimed", contract.claimed());
                contractTag.putBoolean("Accepted", contract.accepted());
                contractTag.putString("RewardToken", contract.rewardToken());
                contractList.add(contractTag);
            }

            playerTag.put("Contracts", contractList);
            playersTag.put(playerEntry.getKey().toString(), playerTag);
        }

        tag.put("Players", playersTag);
        ListTag exploredTag = new ListTag();
        for (String location : exploredLocations) {
            exploredTag.add(net.minecraft.nbt.StringTag.valueOf(location));
        }
        tag.put("ExploredLocations", exploredTag);
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

    private record PlayerContracts(long refreshDay, long refreshVersion, List<ContractEntry> contracts) {
    }

    public record ContractEntry(
            String id,
            String title,
            String description,
            int target,
            long reward,
            int progress,
            boolean claimed,
            boolean accepted,
            String rewardToken
    ) {
        public ContractEntry withClaimed(boolean value) {
            return new ContractEntry(id, title, description, target, reward, progress, value, accepted, rewardToken);
        }

        public ContractEntry withAccepted(boolean value) {
            return new ContractEntry(id, title, description, target, reward, progress, claimed, value, rewardToken);
        }

        public ContractEntry withProgress(int value) {
            return new ContractEntry(id, title, description, target, reward, value, claimed, accepted, rewardToken);
        }
    }
}
