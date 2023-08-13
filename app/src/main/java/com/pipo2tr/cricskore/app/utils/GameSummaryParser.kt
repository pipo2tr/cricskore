package com.pipo2tr.cricskore.app.utils

import org.json.JSONArray
import org.json.JSONObject

// This could probably have been done using kotlin Serialization, but the data was too complex and honestly I cga, so leaving it as TODO
class GameSummaryParser(raw: String) {
    var teams = mutableListOf<TeamInfo>()
    val batsMen = mutableListOf<LiveBatsman>()
    val bowlers = mutableListOf<LiveBowler>()
    var currentOver = mutableListOf<BallInfo>()
    var totalOvers = ""
    var commentary = mutableListOf<CommentaryInfo>()
    var lastWicket = ""
    var partnership = ""
    var description: MatchDescription
    var isYetToBegin: Boolean
    var status: String

    init {
        val data = JSONObject(raw)
        val match = data.getJSONObject("match")
        val live = data.getJSONObject("live")
        val comms = data.getJSONArray("comms")
        isYetToBegin = match.get("match_status") == "dormant"
        createTeamInfo(data)
        if (!isYetToBegin) {
            createTeamScores(data)
            createLiveBatsmanInfo(live)
            createLiveBowlerInfo(live)
            createLAstWicketAndPartnerShipString(live)
            createOverInfo(live)
            createCommentary(comms)
        }
        status = if (live.get("break") == "") {
            live.get("status").toString()
        } else {
            "${live.get("break")}, ${live.get("status")}"
        }
        description = MatchDescription(
            tourney = (data.get("description").toString()).split(",")[0].trim(),
            time = (match.get("start_time_local").toString()).split(":").dropLast(1)
                .joinToString(":"),
            date = (match.get("start_date").toString()).split(",")[0],
            title = match.get("cms_match_title").toString()
        )
    }

    fun getBattingTeam(): TeamInfo {
        val (team1, team2) = this.teams
        if (team1.isBatting) {
            return team1
        }
        return team2
    }

    fun getBowlingTeam(): TeamInfo {
        val (team1, team2) = this.teams
        if (!team1.isBatting) {
            return team1
        }
        return team2
    }

    private fun createTeamInfo(data: JSONObject) {
        val teams = data.getJSONArray("team")
        this.teams.add(TeamInfo(teams.getJSONObject(0)))
        this.teams.add(TeamInfo(teams.getJSONObject(1)))
    }

    private fun createTeamScores(data: JSONObject) {
        val innings = data.get("innings") as JSONArray
        for (i in 0 until innings.length()) {
            val innObj = innings.getJSONObject(i)
            val id = innObj.get("batting_team_id") as Int
            val isFirst = innObj.get("innings_numth") == "1st"
            val isCurrent = innObj.get("live_current_name") == "current innings"
            val inning = Inning(
                runs = innObj.get("runs").toString().toInt(),
                overs = innObj.get("overs").toString(),
                wickets = innObj.get("wickets").toString().toInt(),
                declared = innObj.get("event_name") == "declared"
            )
            if (isCurrent) {
                totalOvers = inning.overs
            }
            val (team1, team2) = this.teams
            if (team1.id == id) {
                team1.setInnings(isFirst, inning, isCurrent)
            } else {
                team2.setInnings(isFirst, inning, isCurrent)
            }
        }
    }

    private fun createCommentary(comms: JSONArray) {
        val length = comms.length()
        for (i in 0 until length) {
//            if (isValidOver || commentary.size > 7) break
            comms.getJSONObject(i).getJSONArray("ball").let {
                for (j in 0 until it.length()) {
                    val currOver = it.getJSONObject(j)
                    if (!currOver.has("overs_actual")) continue
                    val over = currOver.get("overs_actual").toString()
                    var dismissText = ""
                    val event = currOver.has("event").let { hasEvent ->
                        if (hasEvent) {
                            currOver.get("event").toString()
                        } else {
                            ""
                        }
                    }
                    if (event.isBlank()) continue
                    if (event == "OUT") {
                        dismissText = currOver.get("dismissal").toString() + "\n"
                    }
                    val text = currOver.get("text").toString()
                    val players = currOver.get("players").toString()
                    val comm = "$players, $event\n$dismissText$text"
                    commentary.add(CommentaryInfo(over, comm))
                }
            }

        }
    }


    private fun createLiveBatsmanInfo(live: JSONObject) {
        val currentBatting = live.getJSONArray("batting")
        for (i in 0 until currentBatting.length()) {
            val it = currentBatting.getJSONObject(i)
            batsMen.add(
                LiveBatsman(
                    isStriker = it.get("live_current_name") == "striker",
                    runs = it.get("runs").toString().toInt(),
                    balls = it.get("balls_faced").toString().toInt(),
                    name = getBattingTeam().getPlayer(it.get("player_id").toString().toInt())
                )
            )
        }
    }

    private fun createLiveBowlerInfo(live: JSONObject) {
        val currentBowling = live.getJSONArray("bowling")
        for (i in 0 until currentBowling.length()) {
            val it = currentBowling.getJSONObject(i)
            bowlers.add(
                LiveBowler(
                    isBowling = it.get("live_current_name") == "current bowler",
                    runs = it.get("conceded").toString().toInt(),
                    wickets = it.get("wickets").toString().toInt(),
                    name = getBowlingTeam().getPlayer(it.get("player_id").toString().toInt()),
                    overs = it.get("overs").toString()
                )
            )
        }
    }

    private fun createLAstWicketAndPartnerShipString(live: JSONObject) {
        val fow = live.getJSONArray("fow")
        var lastWicket = false
        if (fow.length() == 0) return
        fow.getJSONObject(0)?.let {
            val fowOvers = it.get("fow_overs")
            val fowRuns = it.get("fow_runs")
            val fowWickets = it.get("fow_wickets")
            if (it.get("live_current_name") == "current partnership") {
                val runs = it.get("partnership_runs")
                val overs = it.get("partnership_overs")
                partnership = "Partnership $runs runs of $overs overs"
            }
            if (it.get("live_current_name") == "last wicket") {
                lastWicket = true
                val player = it.get("out_player") as JSONObject
                val name = getBattingTeam().getPlayer(player.get("player_id") as Int)
                val runs = player.get("runs")
                val balls = player.get("balls_faced")
//                val fours = player.get("fours")
//                val sizes = player.get("sixes")
                val dismiss = player.get("dismissal_string")
                this.lastWicket =
                    "Last Wicket $name $dismiss $runs($balls) - $fowRuns/$fowWickets in $fowOvers O"

            }
        }

        if (!lastWicket && fow.length() > 1) {
            fow.getJSONObject(1)?.let {
                val fowOvers = it.get("fow_overs")
                val fowRuns = it.get("fow_runs")
                val fowWickets = it.get("fow_wickets")
                if (it.get("live_current_name") == "last wicket") {
                    val player = it.getJSONObject("out_player")
                    val name = getBattingTeam().getPlayer(player.get("player_id") as Int)
                    val runs = player.get("runs")
                    val balls = player.get("balls_faced")
//                val fours = player.get("fours")
//                val sizes = player.get("sixes")
                    val dismiss = player.get("dismissal_string")
                    this.lastWicket =
                        "Last Wicket $name $dismiss $runs($balls) - $fowRuns/$fowWickets in $fowOvers O"

                }
            }
        }

    }

    private fun createOverInfo(live: JSONObject) {
        val lastOver = live.getJSONArray("recent_overs")
        if (lastOver.length() == 0) return
        lastOver.getJSONArray(lastOver.length() - 1)?.let {
            for (i in 0 until it.length()) {
                val ball = it.getJSONObject(i)
                val extra = ball.get("extras").toString()
                val runs = ball.get("ball").toString()
                val type = if (extra.isNotBlank()) {
                    BallType.EXTRA
                } else if (runs == "&bull;") {
                    BallType.DOT
                } else if (runs.contains("W")) {
                    BallType.WICKET
                } else if (runs.toInt() == 4 || runs.toInt() == 6) {
                    BallType.BOUNDARY
                } else {
                    BallType.RUNS
                }
                val value = if (type == BallType.EXTRA) {
                    "${
                        if (extra.lowercase() == "wd") {
                            runs.toInt() - 1
                        } else {
                            runs.toInt()
                        }
                    }$extra"
                } else {
                    runs
                }
                currentOver.add(BallInfo(value, type))
            }
        }

    }

}

data class MatchDescription(
    val tourney: String, val title: String, var date: String, val time: String
)


data class LiveBatsman(val isStriker: Boolean, val name: String, val runs: Int, val balls: Int)
data class LiveBowler(
    val isBowling: Boolean, val name: String, val runs: Int, val wickets: Int, val overs: String
)

data class BallInfo(val value: String, val type: BallType)
data class Inning(val runs: Int, val wickets: Int, val overs: String, val declared: Boolean)
data class InningScore(val score: String, val overs: String)
data class CommentaryInfo(val over: String, val comm: String)

private val playerMap = mutableMapOf<String, String>()

private const val LOGO_RES_PATH =
    "https://img1.hscicdn.com/image/upload/f_auto,t_ds_square_w_160,q_50/lsci"

class TeamInfo(team: JSONObject) {
    var id: Int = 0
    var name: String
    var logoPath: String
    var abbr: String
    private var innings1: Inning? = null
    private var innings2: Inning? = null
    var isBatting: Boolean = false

    init {
        id = team.get("content_id") as Int
        name = team.get("team_name").toString()
        val teamLogo = team.get("logo_path").toString()
        logoPath = if (teamLogo.isBlank()) {
            ""
        } else {
            LOGO_RES_PATH + teamLogo
        }
        abbr = team.get("team_abbreviation").toString()
        team.has("player").let { hasPlayer ->
            if (hasPlayer) {
                val playerList = team.getJSONArray("player")
                for (i in 0 until playerList.length()) {
                    val player = playerList.getJSONObject(i)
                    val id = player.get("player_id").toString().toInt()
                    val name = player.get("mobile_name").toString().let {
                        it.ifBlank {
                            abbrName(player.get("known_as").toString())
                        }
                    }
                    playerMap[id.toString()] = name
                }
            }
        }
    }

    fun scorecard(): InningScore {
        if (innings1 === null) {
            return InningScore("DNB", "(DNB)")
        }
        if (innings2 == null) {
            val score = "${innings1?.runs}/${innings1?.wickets}"
            val overs = "(${innings1?.overs})"
            return InningScore(score, overs)
        }
        val score = "${innings2?.runs!!}/${innings2?.wickets!!}"
        val overs = "& ${innings1?.runs}/${innings1?.wickets}"
        return InningScore(score, overs)
    }

    fun getPlayer(id: Int): String {
        return playerMap[id.toString()] ?: "Name"
    }

    fun setInnings(iSFirst: Boolean, inning: Inning, current: Boolean) {
        if (iSFirst) {
            innings1 = inning
        } else {
            innings2 = inning
        }
        isBatting = current
    }


}

private fun abbrName(name: String): String {
    if (name.length < 12) return name
    val split = name.split(" ")
    if (split.size < 2) return name
    if (split.size > 2) {
        return split[0].take(1).uppercase() + split[1].take(1).uppercase() + ". " + split[2]
    }
    return split[0].take(1).uppercase() + ". " + split[1]
}