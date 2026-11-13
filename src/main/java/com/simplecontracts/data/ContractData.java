package com.simplecontracts.data;

import com.simpleeconomy.api.EconomyAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public final class ContractData extends SavedData {
    private static final String DATA_NAME = "simplecontracts_contracts";
    public static final int CONTRACTS_PER_LEVEL = 3;
    public static final int MAX_ACTIVE_CONTRACTS = 3;
    private static final SavedData.Factory<ContractData> FACTORY =
            new SavedData.Factory<>(ContractData::new, ContractData::load, DataFixTypes.LEVEL);

    private static final List<ContractTemplate> TEMPLATES = List.of(
            new ContractTemplate("zombie_hunter","Охотник на зомби","Убейте зомби",1,6,10,90,150),
            new ContractTemplate("skeleton_hunter","Охотник на скелетов","Убейте скелетов",1,5,8,90,150),
            new ContractTemplate("iron_miner","Добытчик железа","Добудьте железную руду",1,10,18,90,150),
            new ContractTemplate("wheat_farmer","Фермер","Соберите пшеницу",1,16,28,90,150),
            new ContractTemplate("fisher","Рыбак","Поймайте рыбу",1,4,7,90,150),
            new ContractTemplate("explorer","Исследователь","Посетите новые места",1,3,5,90,150),
            new ContractTemplate("spider_hunter","Охотник на пауков","Убейте пауков",1,6,10,90,150),
            new ContractTemplate("woodcutter","Лесоруб","Срубите брёвна",1,24,40,90,150),
            new ContractTemplate("stone_miner","Каменщик","Добудьте камень",1,24,40,90,150),

            new ContractTemplate("creeper_hunter","Охотник на криперов","Убейте криперов",2,6,10,200,300),
            new ContractTemplate("drowned_hunter","Охотник на утопленников","Убейте утопленников",2,8,12,200,300),
            new ContractTemplate("gold_miner","Золотодобытчик","Добудьте золотую руду",2,8,14,200,300),
            new ContractTemplate("root_farmer","Овощевод","Соберите морковь или картофель",2,20,32,200,300),

            new ContractTemplate("enderman_hunter","Охотник на эндерменов","Убейте эндерменов",3,6,10,400,650),
            new ContractTemplate("diamond_miner","Алмазный старатель","Добудьте алмазную руду",3,5,9,400,650),
            new ContractTemplate("redstone_miner","Мастер редстоуна","Добудьте редстоуновую руду",3,20,32,400,650),
            new ContractTemplate("slime_hunter","Охотник на слизней","Убейте слизней",3,8,14,400,650),

            new ContractTemplate("blaze_hunter","Покоритель Незера","Убейте ифритов",4,8,12,800,1200),
            new ContractTemplate("witch_hunter","Охотник на ведьм","Убейте ведьм",4,5,9,800,1200),
            new ContractTemplate("emerald_miner","Изумрудный старатель","Добудьте изумрудную руду",4,6,10,800,1200),
            new ContractTemplate("obsidian_miner","Обсидиановый мастер","Добудьте обсидиан",4,16,24,800,1200),

            new ContractTemplate("wither_skeleton_hunter","Гроза визер-скелетов","Убейте визер-скелетов",5,6,10,2500,3000),
            new ContractTemplate("guardian_hunter","Охотник на стражей","Убейте стражей",5,8,14,2500,3000),
            new ContractTemplate("ancient_debris_miner","Искатель древних обломков","Добудьте древние обломки",5,3,5,2500,3000),
            new ContractTemplate("warden_hunter","Победитель хранителя","Победите хранителя",5,1,1,2500,3000)
    );

    private final Map<UUID, PlayerContracts> players = new HashMap<>();
    private final Set<String> exploredLocations = new HashSet<>();
    private long globalRefreshDay = Long.MIN_VALUE;
    private long globalRefreshVersion;

    public static ContractData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public void refreshOnlinePlayers(long day, Collection<ServerPlayer> online) {
        for (ServerPlayer player : online) getOrCreateContracts(player.getUUID(), day);
    }

    public int forceRefresh(long day, Collection<ServerPlayer> online) {
        if (day > globalRefreshDay) globalRefreshDay = day;
        globalRefreshVersion++;
        for (ServerPlayer player : online) ensurePlayer(player.getUUID(), globalRefreshDay, globalRefreshVersion);
        setDirty();
        return online.size();
    }

    public List<ContractEntry> getOrCreateContracts(UUID id, long day) {
        if (day > globalRefreshDay) {
            globalRefreshDay = day;
            globalRefreshVersion++;
            setDirty();
        }
        ensurePlayer(id, globalRefreshDay, globalRefreshVersion);
        return getContracts(id);
    }

    public List<ContractEntry> getContracts(UUID id) {
        PlayerContracts data = players.get(id);
        return data == null ? List.of() : List.copyOf(data.contracts());
    }

    public List<ContractEntry> getContracts(UUID id, int level) {
        return getContracts(id).stream().filter(c -> c.level() == level).toList();
    }

    public List<ContractEntry> getActiveContracts(UUID id) {
        return getContracts(id).stream().filter(c -> c.accepted() && !c.claimed()).toList();
    }

    public int getReputation(UUID id) {
        PlayerContracts data = players.get(id);
        return data == null ? 0 : data.reputation();
    }

    public int getPlayerLevel(UUID id) {
        return getLevelForReputation(getReputation(id));
    }

    public static int getLevelForReputation(int points) {
        if (points <= 50) return 1;
        if (points <= 120) return 2;
        if (points <= 250) return 3;
        if (points <= 500) return 4;
        return 5;
    }

    public static int getNextLevelThreshold(int level) {
        return switch (level) {
            case 1 -> 51;
            case 2 -> 121;
            case 3 -> 251;
            case 4 -> 501;
            default -> -1;
        };
    }

    public static int getReputationReward(int level) {
        return switch (level) {
            case 1 -> 5;
            case 2 -> 10;
            case 3 -> 15;
            case 4 -> 20;
            case 5 -> 25;
            default -> 0;
        };
    }

    public boolean canAcceptMore(UUID id) {
        return getActiveContracts(id).size() < MAX_ACTIVE_CONTRACTS;
    }

    public boolean acceptContract(UUID id, int level, int pageIndex) {
        PlayerContracts current = players.get(id);
        if (current == null || level < 1 || level > getLevelForReputation(current.reputation())
                || !canAcceptMore(id)) return false;
        int globalIndex = findIndex(current.contracts(), level, pageIndex);
        if (globalIndex < 0) return false;
        List<ContractEntry> updated = new ArrayList<>(current.contracts());
        ContractEntry contract = updated.get(globalIndex);
        if (contract.accepted() || contract.claimed()) return false;
        updated.set(globalIndex, contract.withAccepted(true));
        players.put(id, current.withContracts(updated));
        setDirty();
        return true;
    }

    public boolean advanceProgress(UUID id, String contractId, int amount) {
        if (amount <= 0) return false;
        PlayerContracts current = players.get(id);
        if (current == null) return false;
        List<ContractEntry> updated = new ArrayList<>(current.contracts());
        for (int i = 0; i < updated.size(); i++) {
            ContractEntry c = updated.get(i);
            if (c.id().equals(contractId) && c.accepted() && !c.claimed() && c.progress() < c.target()) {
                updated.set(i, c.withProgress(Math.min(c.target(), c.progress() + amount)));
                players.put(id, current.withContracts(updated));
                setDirty();
                return true;
            }
        }
        return false;
    }

    public boolean claimReward(ServerPlayer player, int level, int pageIndex) {
        UUID id = player.getUUID();
        PlayerContracts current = players.get(id);
        if (current == null || level < 1 || level > getLevelForReputation(current.reputation())) return false;
        int globalIndex = findIndex(current.contracts(), level, pageIndex);
        if (globalIndex < 0) return false;
        ContractEntry c = current.contracts().get(globalIndex);
        if (!c.accepted() || c.claimed() || c.progress() < c.target()) return false;

        EconomyAPI.addContractReward(player, c.reward(), c.title(), c.rewardToken());
        List<ContractEntry> updated = new ArrayList<>(current.contracts());
        updated.set(globalIndex, c.withClaimed(true));
        int reputation = current.reputation() + getReputationReward(c.level());
        players.put(id, new PlayerContracts(current.refreshDay(), current.refreshVersion(), reputation, updated));
        setDirty();
        ensurePlayer(id, globalRefreshDay, globalRefreshVersion);
        return true;
    }

    public boolean recordExploration(ServerPlayer player) {
        UUID id = player.getUUID();
        if (getActiveContracts(id).stream().noneMatch(c -> c.id().equals("explorer")
                && c.progress() < c.target())) return false;
        int chunkX = player.blockPosition().getX() >> 4;
        int chunkZ = player.blockPosition().getZ() >> 4;
        String key = id + ":" + player.level().dimension().location() + ":" + chunkX + ":" + chunkZ;
        if (!exploredLocations.add(key)) return false;
        setDirty();
        return advanceProgress(id, "explorer", 1);
    }

    private static int findIndex(List<ContractEntry> contracts, int level, int pageIndex) {
        if (pageIndex < 0 || pageIndex >= CONTRACTS_PER_LEVEL) return -1;
        int count = 0;
        for (int i = 0; i < contracts.size(); i++) {
            if (contracts.get(i).level() == level && count++ == pageIndex) return i;
        }
        return -1;
    }

    private void ensurePlayer(UUID id, long day, long version) {
        PlayerContracts current = players.get(id);
        boolean newCycle = current == null || current.refreshDay() < day || current.refreshVersion() < version;
        int reputation = current == null ? 0 : current.reputation();
        int unlocked = getLevelForReputation(reputation);
        if (current != null && !newCycle && hasPages(current.contracts(), unlocked)) return;

        List<ContractEntry> generated = new ArrayList<>();
        Set<String> previousOffers = new HashSet<>();
        if (current != null) {
            for (ContractEntry c : current.contracts()) {
                if (c.accepted() && !c.claimed()) generated.add(c);
                else if (!c.accepted()) previousOffers.add(c.level() + ":" + c.id());
                if (!newCycle && c.claimed()) generated.add(c);
                if (!newCycle && !c.accepted()) generated.add(c);
            }
        }

        for (int level = 1; level <= unlocked; level++) {
            int missing = CONTRACTS_PER_LEVEL - countLevel(generated, level);
            if (missing <= 0) continue;
            List<ContractTemplate> choices = new ArrayList<>(templates(level));
            choices.removeIf(t -> previousOffers.contains(level + ":" + t.id()));
            long seed = id.getMostSignificantBits() ^ id.getLeastSignificantBits()
                    ^ (day * 0x9E3779B97F4A7C15L)
                    ^ (version * 0xD1B54A32D192ED03L)
                    ^ ((long) level * 0x94D049BB133111EBL);
            Random random = new Random(seed);
            Collections.shuffle(choices, random);
            List<ContractTemplate> fallback = new ArrayList<>(templates(level));
            Collections.shuffle(fallback, random);
            for (ContractTemplate t : choices) {
                if (missing <= 0) break;
                if (contains(generated, level, t.id())) continue;
                generated.add(create(t, random));
                missing--;
            }
            for (ContractTemplate t : fallback) {
                if (missing <= 0) break;
                if (contains(generated, level, t.id())) continue;
                generated.add(create(t, random));
                missing--;
            }
        }
        players.put(id, new PlayerContracts(day, version, reputation, generated));
        setDirty();
    }

    private static ContractEntry create(ContractTemplate t, Random random) {
        return new ContractEntry(t.id(), t.level(), t.title(), t.description(),
                random.nextInt(t.targetMin(), t.targetMax() + 1),
                random.nextInt(t.rewardMin(), t.rewardMax() + 1), 0, false, false, UUID.randomUUID().toString());
    }

    private static List<ContractTemplate> templates(int level) {
        return TEMPLATES.stream().filter(t -> t.level() == level).toList();
    }

    private static int countLevel(List<ContractEntry> contracts, int level) {
        return (int) contracts.stream().filter(c -> c.level() == level).count();
    }

    private static boolean hasPages(List<ContractEntry> contracts, int unlocked) {
        for (int level = 1; level <= unlocked; level++) if (countLevel(contracts, level) < CONTRACTS_PER_LEVEL) return false;
        return true;
    }

    private static boolean contains(List<ContractEntry> contracts, int level, String id) {
        return contracts.stream().anyMatch(c -> c.level() == level && c.id().equals(id));
    }

    private ContractData() {}

    private static ContractData load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        ContractData data = new ContractData();
        data.globalRefreshDay = tag.getLong("GlobalRefreshDay");
        data.globalRefreshVersion = tag.getLong("GlobalRefreshVersion");
        ListTag explored = tag.getList("ExploredLocations", Tag.TAG_STRING);
        for (int i = 0; i < explored.size(); i++) data.exploredLocations.add(explored.getString(i));

        CompoundTag playersTag = tag.getCompound("Players");
        for (String key : playersTag.getAllKeys()) {
            try {
                UUID id = UUID.fromString(key);
                CompoundTag playerTag = playersTag.getCompound(key);
                ListTag list = playerTag.getList("Contracts", Tag.TAG_COMPOUND);
                List<ContractEntry> contracts = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag c = list.getCompound(i);
                    String token = c.getString("RewardToken");
                    if (token.isBlank()) token = UUID.randomUUID().toString();
                    contracts.add(new ContractEntry(c.getString("Id"),
                            Math.max(1, c.getInt("Level") == 0 ? 1 : c.getInt("Level")),
                            c.getString("Title"), c.getString("Description"),
                            c.getInt("Target"), c.getLong("Reward"), c.getInt("Progress"),
                            c.getBoolean("Claimed"), c.getBoolean("Accepted"), token));
                }
                data.players.put(id, new PlayerContracts(playerTag.getLong("RefreshDay"),
                        playerTag.getLong("RefreshVersion"), playerTag.getInt("Reputation"), contracts));
            } catch (IllegalArgumentException ignored) {
                // Skip malformed player UUID.
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        tag.putLong("GlobalRefreshDay", globalRefreshDay);
        tag.putLong("GlobalRefreshVersion", globalRefreshVersion);
        CompoundTag playersTag = new CompoundTag();
        for (Map.Entry<UUID, PlayerContracts> entry : players.entrySet()) {
            PlayerContracts p = entry.getValue();
            CompoundTag pt = new CompoundTag();
            pt.putLong("RefreshDay", p.refreshDay());
            pt.putLong("RefreshVersion", p.refreshVersion());
            pt.putInt("Reputation", p.reputation());
            ListTag list = new ListTag();
            for (ContractEntry c : p.contracts()) {
                CompoundTag ct = new CompoundTag();
                ct.putString("Id", c.id());
                ct.putInt("Level", c.level());
                ct.putString("Title", c.title());
                ct.putString("Description", c.description());
                ct.putInt("Target", c.target());
                ct.putLong("Reward", c.reward());
                ct.putInt("Progress", c.progress());
                ct.putBoolean("Claimed", c.claimed());
                ct.putBoolean("Accepted", c.accepted());
                ct.putString("RewardToken", c.rewardToken());
                list.add(ct);
            }
            pt.put("Contracts", list);
            playersTag.put(entry.getKey().toString(), pt);
        }
        tag.put("Players", playersTag);
        ListTag explored = new ListTag();
        for (String location : exploredLocations) explored.add(net.minecraft.nbt.StringTag.valueOf(location));
        tag.put("ExploredLocations", explored);
        return tag;
    }

    private record ContractTemplate(String id, String title, String description, int level,
                                     int targetMin, int targetMax, int rewardMin, int rewardMax) {}

    private record PlayerContracts(long refreshDay, long refreshVersion, int reputation, List<ContractEntry> contracts) {
        private PlayerContracts { contracts = List.copyOf(contracts); }
        private PlayerContracts withContracts(List<ContractEntry> updated) {
            return new PlayerContracts(refreshDay, refreshVersion, reputation, updated);
        }
    }

    public record ContractEntry(String id, int level, String title, String description, int target,
                                long reward, int progress, boolean claimed, boolean accepted, String rewardToken) {
        public ContractEntry withClaimed(boolean value) {
            return new ContractEntry(id, level, title, description, target, reward, progress, value, accepted, rewardToken);
        }
        public ContractEntry withAccepted(boolean value) {
            return new ContractEntry(id, level, title, description, target, reward, progress, claimed, value, rewardToken);
        }
        public ContractEntry withProgress(int value) {
            return new ContractEntry(id, level, title, description, target, reward, value, claimed, accepted, rewardToken);
        }
    }
}
