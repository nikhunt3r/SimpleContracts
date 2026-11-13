package com.simplecontracts.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server-side gameplay settings for Simple Contracts.
 *
 * The file is created in the world's serverconfig directory.
 */
public final class SimpleContractsConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue MAX_ACTIVE_CONTRACTS;
    public static final ModConfigSpec.IntValue REFRESH_INTERVAL_DAYS;
    public static final ModConfigSpec.DoubleValue OBJECTIVE_AMOUNT_MULTIPLIER;

    public static final ModConfigSpec.IntValue LEVEL_1_MAX_REPUTATION;
    public static final ModConfigSpec.IntValue LEVEL_2_MAX_REPUTATION;
    public static final ModConfigSpec.IntValue LEVEL_3_MAX_REPUTATION;
    public static final ModConfigSpec.IntValue LEVEL_4_MAX_REPUTATION;

    public static final ModConfigSpec.IntValue LEVEL_1_MIN_REWARD;
    public static final ModConfigSpec.IntValue LEVEL_1_MAX_REWARD;
    public static final ModConfigSpec.IntValue LEVEL_2_MIN_REWARD;
    public static final ModConfigSpec.IntValue LEVEL_2_MAX_REWARD;
    public static final ModConfigSpec.IntValue LEVEL_3_MIN_REWARD;
    public static final ModConfigSpec.IntValue LEVEL_3_MAX_REWARD;
    public static final ModConfigSpec.IntValue LEVEL_4_MIN_REWARD;
    public static final ModConfigSpec.IntValue LEVEL_4_MAX_REWARD;
    public static final ModConfigSpec.IntValue LEVEL_5_MIN_REWARD;
    public static final ModConfigSpec.IntValue LEVEL_5_MAX_REWARD;

    public static final ModConfigSpec.IntValue LEVEL_1_REPUTATION_REWARD;
    public static final ModConfigSpec.IntValue LEVEL_2_REPUTATION_REWARD;
    public static final ModConfigSpec.IntValue LEVEL_3_REPUTATION_REWARD;
    public static final ModConfigSpec.IntValue LEVEL_4_REPUTATION_REWARD;
    public static final ModConfigSpec.IntValue LEVEL_5_REPUTATION_REWARD;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("general");
        MAX_ACTIVE_CONTRACTS = builder
                .comment("Maximum number of accepted, unclaimed contracts per player across all pages.")
                .translation("config.simplecontracts.max_active_contracts")
                .defineInRange("maxActiveContracts", 3, 1, 15);
        REFRESH_INTERVAL_DAYS = builder
                .comment("Number of in-game days between automatic contract board refreshes.")
                .translation("config.simplecontracts.refresh_interval_days")
                .defineInRange("refreshIntervalDays", 1, 1, 30);
        OBJECTIVE_AMOUNT_MULTIPLIER = builder
                .comment("Scales generated objective counts. 1.0 keeps the built-in target ranges unchanged.")
                .translation("config.simplecontracts.objective_amount_multiplier")
                .defineInRange("objectiveAmountMultiplier", 1.0D, 0.1D, 5.0D);
        builder.pop();

        builder.push("reputation");
        LEVEL_1_MAX_REPUTATION = reputationThreshold(builder, "level1Max", "Highest reputation score for Beginner.", 50);
        LEVEL_2_MAX_REPUTATION = reputationThreshold(builder, "level2Max", "Highest reputation score for Advanced.", 120);
        LEVEL_3_MAX_REPUTATION = reputationThreshold(builder, "level3Max", "Highest reputation score for Experienced.", 250);
        LEVEL_4_MAX_REPUTATION = reputationThreshold(builder, "level4Max", "Highest reputation score for Professional.", 500);

        LEVEL_1_REPUTATION_REWARD = reputationReward(builder, "level1Reward", 5);
        LEVEL_2_REPUTATION_REWARD = reputationReward(builder, "level2Reward", 10);
        LEVEL_3_REPUTATION_REWARD = reputationReward(builder, "level3Reward", 15);
        LEVEL_4_REPUTATION_REWARD = reputationReward(builder, "level4Reward", 20);
        LEVEL_5_REPUTATION_REWARD = reputationReward(builder, "level5Reward", 25);
        builder.pop();

        builder.push("moneyRewards");
        LEVEL_1_MIN_REWARD = rewardValue(builder, "level1Minimum", "Minimum money reward for Beginner contracts.", 90);
        LEVEL_1_MAX_REWARD = rewardValue(builder, "level1Maximum", "Maximum money reward for Beginner contracts.", 150);
        LEVEL_2_MIN_REWARD = rewardValue(builder, "level2Minimum", "Minimum money reward for Advanced contracts.", 200);
        LEVEL_2_MAX_REWARD = rewardValue(builder, "level2Maximum", "Maximum money reward for Advanced contracts.", 300);
        LEVEL_3_MIN_REWARD = rewardValue(builder, "level3Minimum", "Minimum money reward for Experienced contracts.", 400);
        LEVEL_3_MAX_REWARD = rewardValue(builder, "level3Maximum", "Maximum money reward for Experienced contracts.", 650);
        LEVEL_4_MIN_REWARD = rewardValue(builder, "level4Minimum", "Minimum money reward for Professional contracts.", 800);
        LEVEL_4_MAX_REWARD = rewardValue(builder, "level4Maximum", "Maximum money reward for Professional contracts.", 1200);
        LEVEL_5_MIN_REWARD = rewardValue(builder, "level5Minimum", "Minimum money reward for Legend contracts.", 2500);
        LEVEL_5_MAX_REWARD = rewardValue(builder, "level5Maximum", "Maximum money reward for Legend contracts.", 3000);
        builder.pop();

        SPEC = builder.build();
    }

    private SimpleContractsConfig() {
    }

    private static ModConfigSpec.IntValue reputationThreshold(
            ModConfigSpec.Builder builder, String name, String comment, int defaultValue
    ) {
        return builder.comment(comment)
                .translation("config.simplecontracts." + name)
                .defineInRange(name, defaultValue, 0, 1_000_000);
    }

    private static ModConfigSpec.IntValue reputationReward(
            ModConfigSpec.Builder builder, String name, int defaultValue
    ) {
        return builder.comment("Reputation points granted when a contract of this level is claimed.")
                .translation("config.simplecontracts." + name)
                .defineInRange(name, defaultValue, 0, 10_000);
    }

    private static ModConfigSpec.IntValue rewardValue(
            ModConfigSpec.Builder builder, String name, String comment, int defaultValue
    ) {
        return builder.comment(comment)
                .translation("config.simplecontracts." + name)
                .defineInRange(name, defaultValue, 0, 1_000_000);
    }

    public static int getMaxActiveContracts() {
        return MAX_ACTIVE_CONTRACTS.get();
    }

    public static int getRefreshIntervalDays() {
        return REFRESH_INTERVAL_DAYS.get();
    }

    public static double getObjectiveAmountMultiplier() {
        return OBJECTIVE_AMOUNT_MULTIPLIER.get();
    }

    public static int getLevelMaxReputation(int level) {
        int level1 = LEVEL_1_MAX_REPUTATION.get();
        int level2 = Math.max(level1 + 1, LEVEL_2_MAX_REPUTATION.get());
        int level3 = Math.max(level2 + 1, LEVEL_3_MAX_REPUTATION.get());
        int level4 = Math.max(level3 + 1, LEVEL_4_MAX_REPUTATION.get());
        return switch (level) {
            case 1 -> level1;
            case 2 -> level2;
            case 3 -> level3;
            case 4 -> level4;
            default -> Integer.MAX_VALUE;
        };
    }

    public static int getReputationReward(int level) {
        return switch (level) {
            case 1 -> LEVEL_1_REPUTATION_REWARD.get();
            case 2 -> LEVEL_2_REPUTATION_REWARD.get();
            case 3 -> LEVEL_3_REPUTATION_REWARD.get();
            case 4 -> LEVEL_4_REPUTATION_REWARD.get();
            case 5 -> LEVEL_5_REPUTATION_REWARD.get();
            default -> 0;
        };
    }

    public static int getMinimumMoneyReward(int level) {
        return Math.min(getConfiguredMinimum(level), getConfiguredMaximum(level));
    }

    public static int getMaximumMoneyReward(int level) {
        return Math.max(getConfiguredMinimum(level), getConfiguredMaximum(level));
    }

    private static int getConfiguredMinimum(int level) {
        return switch (level) {
            case 1 -> LEVEL_1_MIN_REWARD.get();
            case 2 -> LEVEL_2_MIN_REWARD.get();
            case 3 -> LEVEL_3_MIN_REWARD.get();
            case 4 -> LEVEL_4_MIN_REWARD.get();
            case 5 -> LEVEL_5_MIN_REWARD.get();
            default -> 0;
        };
    }

    private static int getConfiguredMaximum(int level) {
        return switch (level) {
            case 1 -> LEVEL_1_MAX_REWARD.get();
            case 2 -> LEVEL_2_MAX_REWARD.get();
            case 3 -> LEVEL_3_MAX_REWARD.get();
            case 4 -> LEVEL_4_MAX_REWARD.get();
            case 5 -> LEVEL_5_MAX_REWARD.get();
            default -> 0;
        };
    }
}
