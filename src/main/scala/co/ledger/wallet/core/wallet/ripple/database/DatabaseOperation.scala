package co.ledger.wallet.core.wallet.ripple.database

import co.ledger.wallet.core.wallet.ripple.{Account, Operation, Transaction}
import co.ledger.wallet.web.ripple.content.{OperationModel, TransactionModel}

/**
  * Created by alix on 4/19/17.
  */
class DatabaseOperation( operation: OperationModel,
                         override val account: Account,
                         override val `type`: String,
                         transactionModel: TransactionModel) extends Operation {

  override val transaction: Transaction = transactionModel.proxy

  }

object DatabaseOperation {
  def apply(operation: OperationModel)(account: Account)(transactionModel: TransactionModel): DatabaseOperation = {
    new DatabaseOperation(operation, account,"payment" , transactionModel)
  }
}