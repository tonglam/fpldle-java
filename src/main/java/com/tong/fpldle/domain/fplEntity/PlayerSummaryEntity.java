package com.tong.fpldle.domain.fplEntity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2022/2/17
 */
@Data
@Accessors(chain = true)
public class PlayerSummaryEntity {

    private Integer id;
    private Integer element;
    private Integer fixtureId;
    private Integer event;
    private Integer code;
    private Integer price;
    private Integer teamId;
    private Integer againstTeamId;
    private Integer selected;
    private Integer totalPoints;
    private Integer minutes;
    private Integer goalsScored;
    private Integer assists;
    private Integer cleanSheets;
    private Integer goalsConceded;
    private Integer ownGoals;
    private Integer penaltiesSave;
    private Integer penaltiesMissed;
    private Integer yellowCards;
    private Integer redCards;
    private Integer saves;
    private Integer bonus;
    private Integer bps;
    private String influence;
    private String creativity;
    private String threat;
    private String ictIndex;
    private Integer transfersBalance;
    private Integer transfersIn;
    private Integer transfersOut;

}
