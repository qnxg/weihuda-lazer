package cn.qnxg.weihuda.core.demo

import cn.qnxg.weihuda.core.model.CourseItem
import java.time.LocalDate

/**
 * 演示课表：没有配置后端令牌时使用，保证小组件装好即可看到内容。
 *
 * 课程全部来自计算机专业常见大一/大二课表，覆盖「连续小节合并」「单双周之外的
 * 周次过滤」「周一到周五」等展示分支。
 */
object DemoData {
	/** 让今天固定落在第 9 周附近，方便截图与自测。 */
	fun semesterStart(today: LocalDate): LocalDate = today.minusWeeks(8)

	fun courses(): List<CourseItem> = listOf(
		course("高等数学（一）", "GE01101", "必修", "复临舍101", "南校区", "张伟", "周一", 1, weeks = 1..16, credit = 5.0f),
		course("高等数学（一）", "GE01101", "必修", "复临舍101", "南校区", "张伟", "周一", 2, weeks = 1..16, credit = 5.0f),
		course("大学英语（三）", "GE02203", "必修", "中楼302", "南校区", "李娜", "周一", 3, weeks = 1..16, credit = 2.0f),
		course("大学英语（三）", "GE02203", "必修", "中楼302", "南校区", "李娜", "周一", 4, weeks = 1..16, credit = 2.0f),
		course("数据结构", "CS20301", "必修", "软件大楼401", "南校区", "王强", "周二", 1, weeks = 1..16, credit = 4.0f),
		course("数据结构", "CS20301", "必修", "软件大楼401", "南校区", "王强", "周二", 2, weeks = 1..16, credit = 4.0f),
		course("概率论与数理统计", "MA10204", "必修", "复临舍203", "南校区", "陈静", "周二", 5, weeks = 1..16, credit = 3.0f),
		course("概率论与数理统计", "MA10204", "必修", "复临舍203", "南校区", "陈静", "周二", 6, weeks = 1..16, credit = 3.0f),
		course("计算机组成原理", "CS20402", "必修", "软件大楼205", "南校区", "刘洋", "周三", 3, weeks = 1..16, credit = 4.0f),
		course("计算机组成原理", "CS20402", "必修", "软件大楼205", "南校区", "刘洋", "周三", 4, weeks = 1..16, credit = 4.0f),
		course("体育（羽毛球）", "PE10107", "必修", "南校区体育馆", "南校区", "赵磊", "周三", 7, weeks = 1..16, credit = 1.0f),
		course("体育（羽毛球）", "PE10107", "必修", "南校区体育馆", "南校区", "赵磊", "周三", 8, weeks = 1..16, credit = 1.0f),
		course("操作系统", "CS30501", "必修", "软件大楼301", "南校区", "孙敏", "周四", 1, weeks = 1..16, credit = 4.0f),
		course("操作系统", "CS30501", "必修", "软件大楼301", "南校区", "孙敏", "周四", 2, weeks = 1..16, credit = 4.0f),
		course("中国近现代史纲要", "GE03302", "必修", "中楼105", "南校区", "周涛", "周四", 9, weeks = 1..16, credit = 3.0f),
		course("中国近现代史纲要", "GE03302", "必修", "中楼105", "南校区", "周涛", "周四", 10, weeks = 1..16, credit = 3.0f),
		course("编译原理", "CS30603", "必修", "软件大楼402", "南校区", "吴迪", "周五", 3, weeks = 1..8, credit = 3.0f),
		course("编译原理", "CS30603", "必修", "软件大楼402", "南校区", "吴迪", "周五", 4, weeks = 1..8, credit = 3.0f),
		course("大学生创新实验", "CS40901", "选修", "实验楼201", "南校区", "郑华", "周五", 5, weeks = 9..16, credit = 1.5f),
		course("大学生创新实验", "CS40901", "选修", "实验楼201", "南校区", "郑华", "周五", 6, weeks = 9..16, credit = 1.5f),
	)

	private fun course(
		name: String,
		courseId: String,
		type: String,
		place: String,
		area: String,
		teacher: String,
		dayLabel: String,
		time: Int,
		weeks: IntRange,
		credit: Float,
	) = CourseItem(
		courseName = name,
		courseId = courseId,
		type = type,
		className = "计科2301",
		place = place,
		area = area,
		teacher = teacher,
		weeks = weeks.toList(),
		day = DAY_LABELS.getValue(dayLabel),
		time = time,
		credit = credit,
		customizeId = -1,
		people = 56,
	)

	private val DAY_LABELS = mapOf(
		"周一" to 1,
		"周二" to 2,
		"周三" to 3,
		"周四" to 4,
		"周五" to 5,
		"周六" to 6,
		"周日" to 7,
	)

	const val DEMO_USER_NAME = "演示同学"
}
