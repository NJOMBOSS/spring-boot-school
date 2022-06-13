package org.worldline.springbootschool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.optaplanner.core.api.solver.SolverStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.worldline.springbootschool.domain.Lesson;
import org.worldline.springbootschool.domain.TimeTable;
import org.worldline.springbootschool.rest.TimeTableController;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
		// Effectively disable spent-time termination in favor of the best-score-limit
		"optaplanner.solver.termination.spent-limit=1h",
		"optaplanner.solver.termination.best-score-limit=0hard/*soft"})
class SpringBootSchoolApplicationTests {

	@Autowired
	private TimeTableController timeTableController;

	@Test
	@Timeout(600_000)
	public void solveDemoDataUntilFeasible() throws InterruptedException {
		timeTableController.solve();
		TimeTable timeTable;
		do { // Use do-while to give the solver some time and avoid retrieving an early infeasible solution.
			// Quick polling (not a Test Thread Sleep anti-pattern)
			// Test is still fast on fast machines and doesn't randomly fail on slow machines.
			Thread.sleep(20L);
			timeTable = timeTableController.getTimeTable();
		} while (timeTable.getSolverStatus() != SolverStatus.NOT_SOLVING || !timeTable.getScore().isFeasible());

		assertFalse(timeTable.getLessonList().isEmpty());
		for (Lesson lesson : timeTable.getLessonList()) {
			assertNotNull(lesson.getTimeslot());
			assertNotNull(lesson.getRoom());
		}
		assertTrue(timeTable.getScore().isFeasible());
	}
}
