package com.depromeet.muyaho.domain.memberstock;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable

public class MemberStockAmount {

    @Column(nullable = false)
    private int purchasePrice;

    @Column(nullable = false)
    private int quantity;

    private MemberStockAmount(int purchasePrice, int quantity) {
        validateStockInfo(purchasePrice, quantity);
        this.purchasePrice = purchasePrice;
        this.quantity = quantity;
    }

    private void validateStockInfo(int purchasePrice, int quantity) {
        if (purchasePrice < 0 || quantity < 0) {
            throw new IllegalArgumentException(String.format("주식의 평단가 (%s)와 보유 수량 (%s)은 0보다 작을 수 없습니다", purchasePrice, quantity));
        }
    }

    public static MemberStockAmount of(int purchasePrice, int quantity) {
        return new MemberStockAmount(purchasePrice, quantity);
    }

}