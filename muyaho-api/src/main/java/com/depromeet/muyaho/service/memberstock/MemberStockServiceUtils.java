package com.depromeet.muyaho.service.memberstock;

import com.depromeet.muyaho.domain.memberstock.MemberStock;
import com.depromeet.muyaho.domain.memberstock.MemberStockRepository;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class MemberStockServiceUtils {

    static void validateNotExistStockInMember(MemberStockRepository memberStockRepository, Long stockId, Long memberId) {
        MemberStock memberStock = memberStockRepository.findByMemberIdAndStockId(memberId, stockId);
        if (memberStock != null) {
            throw new IllegalArgumentException(String.format("멤버 (%s)는 해당하는 주식 (%s)을 이미 소유하고 있습니다", memberId, stockId));
        }
    }

    public static MemberStock findMemberStockByIdAndMemberId(MemberStockRepository memberStockRepository, Long memberStockId, Long memberId) {
        MemberStock memberStock = memberStockRepository.findByIdAndMemberId(memberStockId, memberId);
        if (memberStock == null) {
            throw new IllegalArgumentException(String.format("멤버 (%s)에게 해당하는 소유 주식 (%s)이 존재하지 않습니다", memberId, memberStockId));
        }
        return memberStock;
    }

}
