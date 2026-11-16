package cn.qnxg.weihuda.core.usecase

/**
 * 输入端口：刷新「今日课程」快照。
 *
 * 界面、Worker、测试都只认这个接口，谁也看不见适配器。
 */
interface RefreshTodayCourses {
	/**
	 * 取一次数据并写入快照，返回写进去的内容。
	 *
	 * 失败被消化成带 [TodayCourses.failure] 的正常返回，不抛异常：调用方是后台任务，
	 * 抛出去只会变成 WorkManager 的重试风暴，而用户看到的东西没有区别。
	 */
	suspend fun refresh(): TodayCourses
}
