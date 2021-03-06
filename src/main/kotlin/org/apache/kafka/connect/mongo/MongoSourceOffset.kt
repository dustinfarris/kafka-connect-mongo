package org.apache.kafka.connect.mongo

import org.bson.BsonTimestamp
import org.bson.types.ObjectId
import java.lang.Integer.parseInt
import java.util.*

/**
 * Created by jentle on 9/27/17.
 */
class MongoSourceOffset(offsetStr: String?) {

    private val pieces = offsetStr?.split(MongoSourceOffset.SPLITOR.toRegex())

    private val timestamp: Int = pieces?.let { parseInt(it[0]) } ?: Math.floor(Date().time.toDouble() / 1000).toInt()
    private val inc: Int = pieces?.let { if (it.size > 1) parseInt(it[1]) else 0 } ?: 0
    val ts = BsonTimestamp(timestamp, inc)

    // To be compatible with old format
    val objectId: ObjectId = if (pieces != null && pieces.size > 2) ObjectId(pieces[2]) else ObjectId("000000000000000000000000")
    val finishedImport: Boolean = if (pieces != null && pieces.size > 3) parseInt(pieces[3]) > 0 else pieces != null

    override fun toString(): String {
        return MongoSourceOffset.toOffsetString(ts, objectId, finishedImport)
    }

    companion object {
        val SPLITOR = ","

        /**
         * Start from current time will skip a lot of redundant scan on oplog
         * Format: LATEST_TIMESTAMP,INC,OBJECT_ID,FINISH_IMPORT
         */
        fun toOffsetString(ts: BsonTimestamp, objectId: ObjectId, finishedImport: Boolean): String {
            val finishedFlag = if (finishedImport) 1 else -1
            return "${ts.time}$SPLITOR${ts.inc}$SPLITOR$objectId$SPLITOR$finishedFlag"
        }
    }
}
