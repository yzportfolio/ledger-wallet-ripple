package co.ledger.wallet.web.ripple.wallet

import co.ledger.wallet.core.concurrent.AsyncCursor
import co.ledger.wallet.core.utils.DerivationPath
import co.ledger.wallet.core.wallet.ripple._
import co.ledger.wallet.core.wallet.ripple.api.ApiAccountRestClient
import co.ledger.wallet.core.wallet.ripple.database.AccountRow
import co.ledger.wallet.web.ripple.core.net.JQHttpClient
import co.ledger.wallet.web.ripple.services.SessionService
import co.ledger.wallet.web.ripple.wallet.database.RippleDatabase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by alix on 4/13/17.
  */
class RippleAccountClient(walletClient: RippleWalletClient,
                         row: AccountRow
                         ) extends Account {

  private def init(): Future[Unit] = {
    synchronize()
  }

  override def index: Int = row.index

  override def wallet: Wallet = walletClient.asInstanceOf[Wallet]

  override def synchronize(): Future[Unit] = {
    println("Synchronizing account")
    _api.balance() flatMap {(bal) =>
      println("balance=")
      println(bal)
      walletClient.putAccount(new AccountRow(row.index, row.rippleAccount, bal))
      println(s"account ${row.index} updated")
      _api.transactions() map {(transactions) =>
        for (transaction <- transactions) {
          println("transaction")
          println("transaction2")
          walletClient.putTransaction(transaction)
          walletClient.putOperation(new AccountRow(row.index, row
            .rippleAccount, bal),transaction)
        }
      }
    }
  }

  override def isSynchronizing(): Future[Boolean] = ???

  override def operations(limit: Int, batchSize: Int): Future[AsyncCursor[Operation]] = ???

  override def rippleAccount(): Future[RippleAccount] =
    Future.successful(RippleAccount(row.rippleAccount))

  override def rippleAccountDerivationPath(): Future[DerivationPath] =
    Future.successful(DerivationPath(s"44'/${walletClient
      .bip44CoinType}'/$index'/0/0"))

  override def hashCode(): Int = super.hashCode()

  override def balance(): Future[XRP] = {
    walletClient.queryAccount(index) map {(account) =>
      account.balance
    } recover {
      case walletClient.BadAccountIndex() => XRP.Zero
    }
  }

  private val _api = new ApiAccountRestClient(JQHttpClient.xrpInstance,row)
  private var _synchronizationFuture: Option[Future[Unit]] = None
}

