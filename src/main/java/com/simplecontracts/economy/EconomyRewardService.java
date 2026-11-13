package com.simplecontracts.economy;

import com.simpleeconomy.api.EconomyAPI;
import net.minecraft.server.level.ServerPlayer;

/**
 * The only Simple Economy integration point used by Simple Contracts.
 * Call this from the logical server thread after the contract completion
 * has been validated and marked as claimed.
 */
public final class EconomyRewardService {
    private EconomyRewardService() {
    }

    public static long payContractReward(
            ServerPlayer player,
            long amount,
            String contractName
    ) {
        return EconomyAPI.addContractReward(player, amount, contractName);
    }
}
