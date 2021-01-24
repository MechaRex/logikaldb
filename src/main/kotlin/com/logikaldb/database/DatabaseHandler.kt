/*Copyright 2021 Mecharex Kft.
This file is part of the logikaldb library.

The logikaldb library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The logikaldb library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with the logikaldb library. If not, see <http://www.gnu.org/licenses/>.*/

package com.logikaldb.database

import com.apple.foundationdb.FDB
import com.apple.foundationdb.Range
import com.apple.foundationdb.directory.DirectoryLayer
import com.apple.foundationdb.tuple.Tuple
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future

internal class DatabaseHandler(private val fdbVersion: Int = 620, private val clusterFilePath: String? = null) {

    @ExperimentalCoroutinesApi
    fun read(directoryPath: List<PathPart>, key: Key): Flow<Value?> = channelFlow {
        val fdb = FDB.selectAPIVersion(fdbVersion)
        fdb.open(clusterFilePath).use { db ->
            val directory = DirectoryLayer().open(db, directoryPath).await()
            val finalKey = Tuple.fromBytes(directory.pack()).add(key).pack()
            val result = db.readAsync { transaction -> transaction.get(finalKey) }.await()
            send(result?.let { Tuple.fromBytes(it).getBytes(0) })
        }
    }

    @ExperimentalCoroutinesApi
    fun readRange(directoryPath: List<PathPart>, beginKey: Key, endKey: Key): Flow<RangeResult> = channelFlow {
        val fdb = FDB.selectAPIVersion(fdbVersion)
        fdb.open(clusterFilePath).use { db ->
            val directory = DirectoryLayer().open(db, directoryPath).await()
            val finalBeginKey = Tuple.fromBytes(directory.pack()).add(beginKey).pack()
            val finalEndKey = Tuple.fromBytes(directory.pack()).add(endKey).pack()
            val range = Range(finalBeginKey, finalEndKey)
            db.readAsync { transaction ->
                future {
                    val iterator = transaction.getRange(range).iterator()
                    while (iterator.onHasNext().await()) {
                        val item = iterator.next()
                        val key = Tuple.fromBytes(item.key).getString(1)
                        val value = Tuple.fromBytes(item.value).getBytes(0)
                        send(key to value)
                    }
                }
            }
        }
    }

    suspend fun write(directoryPath: List<PathPart>, key: Key, value: Value): Unit = coroutineScope {
        val fdb = FDB.selectAPIVersion(fdbVersion)
        fdb.open(clusterFilePath).use { db ->
            val directory = DirectoryLayer().createOrOpen(db, directoryPath).await()
            val finalKey = Tuple.fromBytes(directory.pack()).add(key).pack()
            val finalValue = Tuple.from(value).pack()
            db.runAsync { transaction ->
                future { transaction.set(finalKey, finalValue) }
            }.await()
        }
    }
}
