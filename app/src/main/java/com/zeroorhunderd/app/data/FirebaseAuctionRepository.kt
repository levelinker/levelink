package com.zeroorhunderd.app.data
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.zeroorhunderd.app.domain.model.Auction
import com.zeroorhunderd.app.domain.model.PurchaseOutcome
import com.zeroorhunderd.app.domain.repository.AuctionRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseAuctionRepository(
    private val userIdProvider: UserIdProvider
) : AuctionRepository {
    private val auctionsRef = FirebaseDatabase.getInstance().reference.child("auctions")
    private val serverTimeOffsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset")

    override fun observeAuctions(): Flow<List<Auction>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val auctions = snapshot.children.mapNotNull { child ->
                    val entity = child.getValue(AuctionEntity::class.java) ?: return@mapNotNull null
                    Auction(
                        auctionId = child.key.orEmpty(),
                        itemId = entity.itemId.orEmpty(),
                        startPrice = entity.startPrice ?: 0.0,
                        bottomPrice = entity.bottomPrice ?: 0.0,
                        startTimestampMs = entity.startTimestamp ?: 0L,
                        durationMs = entity.durationMs ?: 1L,
                        winnerUid = entity.winner,
                        winnerPrice = entity.winnerPrice,
                        imageUrl = entity.imageUrl
                    )
                }
                trySend(auctions)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        auctionsRef.addValueEventListener(listener)
        awaitClose { auctionsRef.removeEventListener(listener) }
    }

    override fun observeAuction(auctionId: String): Flow<Auction> = callbackFlow {
        val ref = auctionsRef.child(auctionId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entity = snapshot.getValue(AuctionEntity::class.java) ?: AuctionEntity()
                val auction = Auction(
                    auctionId = auctionId,
                    itemId = entity.itemId.orEmpty(),
                    startPrice = entity.startPrice ?: 0.0,
                    bottomPrice = entity.bottomPrice ?: 0.0,
                    startTimestampMs = entity.startTimestamp ?: 0L,
                    durationMs = entity.durationMs ?: 1L,
                    winnerUid = entity.winner,
                    winnerPrice = entity.winnerPrice,
                    imageUrl = entity.imageUrl
                )
                trySend(auction)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override fun observeServerTimeOffsetMs(): Flow<Long> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offset = (snapshot.value as? Number)?.toLong() ?: 0L
                trySend(offset)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        serverTimeOffsetRef.addValueEventListener(listener)
        awaitClose { serverTimeOffsetRef.removeEventListener(listener) }
    }

    override suspend fun attemptPurchase(auctionId: String, userUid: String, price: Double): PurchaseOutcome {
        val auctionRef = auctionsRef.child(auctionId)
        return suspendCancellableCoroutine { continuation ->
            auctionRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val winner = currentData.child("winner").value
                    if (winner == null) {
                        currentData.child("winner").value = userUid
                        currentData.child("winnerPrice").value = price
                        return Transaction.success(currentData)
                    }
                    return Transaction.abort()
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (continuation.isCompleted) return
                    when {
                        error != null -> continuation.resume(PurchaseOutcome.Failure(error.message))
                        committed -> continuation.resume(PurchaseOutcome.Success)
                        else -> continuation.resume(PurchaseOutcome.SoldOut)
                    }
                }
            })
        }
    }

    override suspend fun createAuction(auction: Auction) {
        val entity = AuctionEntity(
            itemId = auction.itemId,
            startPrice = auction.startPrice,
            bottomPrice = auction.bottomPrice,
            startTimestamp = auction.startTimestampMs,
            durationMs = auction.durationMs,
            winner = null,
            imageUrl = auction.imageUrl
        )
        auctionsRef.child(auction.auctionId).setValue(entity)
    }

    fun getOrCreateUid(): String = userIdProvider.getOrCreateUid()
}
