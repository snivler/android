package com.felipecsl.elifut.match;

import android.os.Build;

import com.felipecsl.elifut.BuildConfig;
import com.felipecsl.elifut.ElifutTestRunner;
import com.felipecsl.elifut.TestElifutApplication;
import com.felipecsl.elifut.models.Club;
import com.felipecsl.elifut.models.Goal;
import com.felipecsl.elifut.models.LeagueRound;
import com.felipecsl.elifut.models.Match;
import com.felipecsl.elifut.models.MatchResult;
import com.felipecsl.elifut.preferences.JsonPreference;
import com.felipecsl.elifut.preferences.LeaguePreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(ElifutTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP,
    manifest = ElifutTestRunner.MANIFEST_PATH)
public class LeagueRoundExecutorTest {
  private final Club clubA = Club.create(1, "Club A");
  private final Club clubB = Club.create(2, "Club B");
  private final Club clubC = Club.create(3, "Club C");
  private final Club clubD = Club.create(4, "Club D");
  private final List<Club> leagueClubs = Arrays.asList(clubA, clubB, clubC, clubD);

  @Inject LeaguePreferences leaguePreferences;
  @Mock MatchResultGenerator generator;

  private LeagueRoundExecutor executor;
  private JsonPreference<List<Club>> clubsPreference;

  @Before public void setUp() {
    TestElifutApplication application = (TestElifutApplication) RuntimeEnvironment.application;
    application.testComponent().inject(this);
    MockitoAnnotations.initMocks(this);
    clubsPreference = leaguePreferences.clubsPreference();
    clubsPreference.set(leagueClubs);
    executor = new LeagueRoundExecutor(leaguePreferences, generator);
  }

  @Test public void testExecute() throws Exception {
    Match match1 = Match.create(clubA, clubB);
    Match match2 = Match.create(clubC, clubD);

    when(generator.generate(match1)).thenReturn(MatchResult.builder()
        .match(match1)
        .awayGoals(Collections.emptyList())
        .homeGoals(Collections.singletonList(Goal.create(10, clubA)))
        .build());

    when(generator.generate(match2)).thenReturn(MatchResult.builder()
        .match(match2)
        .homeGoals(Collections.emptyList())
        .awayGoals(Arrays.asList(Goal.create(30, clubD), Goal.create(40, clubD)))
        .build());

    executor.execute(LeagueRound.create(1, Arrays.asList(match1, match2)));

    List<Club> clubs = clubsPreference.get();

    assertThat(clubs).containsOnly(
        clubA.newWithWin(), clubB.newWithLoss(), clubC.newWithLoss(), clubD.newWithWin());
  }
}