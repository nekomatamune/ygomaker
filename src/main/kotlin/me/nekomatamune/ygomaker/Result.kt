package me.nekomatamune.ygomaker

fun success(): Result<Unit> = success(Unit)
fun <T> success(value: T?): Result<T> = Result(value, null)
fun failure(error: Exception): Result<Nothing> = Result(null, error)

class Result<out T> internal constructor(
		private val value: T?,
		private val error: Exception?
) {

	fun value() = value!!
	fun isSuccess() = (error == null)
	fun isFailure() = !isSuccess()

	override fun toString() =
			if (isSuccess()) "Success($value)"
			else "Failure($error)"

	inline fun onFailure(ret: (failure: Result<Nothing>) -> Nothing): T {
		@Suppress("UNCHECKED_CAST")
		return if (isFailure()) ret(this as Result<Nothing>) else value()
	}

	fun logFailure(log: (str: String) -> Unit): Result<T> {
		if (isFailure()) {
			log(this.toString())
		}
		return this
	}

}