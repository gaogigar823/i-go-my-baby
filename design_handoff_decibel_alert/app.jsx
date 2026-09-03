// app.jsx — 데시벨 기반 알림 (Soundwatch) 앱 화면 컴포넌트
// 청각 장애 부모를 위한 아기 울음 감지 + 웨어러블 진동 알림 앱

const t = (lightDark) => ({
  // 따뜻한 크림 / 차분한 다크
  bg:           lightDark === 'dark' ? '#1a1815' : '#FAF6F0',
  surface:      lightDark === 'dark' ? '#252220' : '#FFFCF7',
  surfaceAlt:   lightDark === 'dark' ? '#2f2b27' : '#F2EBE0',
  surfaceSunken:lightDark === 'dark' ? '#1f1d1a' : '#EFE7DA',
  border:       lightDark === 'dark' ? '#3a3631' : '#E8DFCF',
  onSurface:    lightDark === 'dark' ? '#F5EFE3' : '#2C2823',
  onSurfaceMed: lightDark === 'dark' ? '#C8C0B0' : '#6B6258',
  onSurfaceDim: lightDark === 'dark' ? '#8C8678' : '#9C9385',
  // 세이지 그린 프라이머리 (안정감, 안심)
  primary:      lightDark === 'dark' ? '#A3D4C5' : '#5C9989',
  primaryDeep:  lightDark === 'dark' ? '#7FB5A9' : '#3F7868',
  primaryFaint: lightDark === 'dark' ? '#2c3e39' : '#DFEFE9',
  primaryOn:    lightDark === 'dark' ? '#0d2521' : '#FFFFFF',
  // 코랄 — 임계값 초과 알림
  alert:        lightDark === 'dark' ? '#F0A89B' : '#E07A6A',
  alertFaint:   lightDark === 'dark' ? '#3a2622' : '#FBE3DD',
  // 옐로우 — 주의(중간)
  warn:         lightDark === 'dark' ? '#E8C26B' : '#D4A93E',
  warnFaint:    lightDark === 'dark' ? '#352d18' : '#FBF1D5',
  shadow:       lightDark === 'dark' ? '0 1px 2px rgba(0,0,0,.4)' : '0 1px 2px rgba(60, 45, 25, .06)',
  shadowLg:     lightDark === 'dark' ? '0 8px 24px rgba(0,0,0,.5)' : '0 8px 24px rgba(60, 45, 25, .08)',
});

const KFONT = '"Pretendard","Pretendard Variable",-apple-system,BlinkMacSystemFont,"Apple SD Gothic Neo","Malgun Gothic",sans-serif';

// ─── 아이콘 (M3 Outlined 스타일, SVG로 간단히) ───
const Icon = ({ name, size = 24, color = 'currentColor', stroke = 1.8 }) => {
  const props = { width: size, height: size, viewBox: '0 0 24 24', fill: 'none', stroke: color, strokeWidth: stroke, strokeLinecap: 'round', strokeLinejoin: 'round' };
  const paths = {
    chevronLeft:  <path d="M15 18l-6-6 6-6" />,
    chevronRight: <path d="M9 18l6-6-6-6" />,
    chevronDown:  <path d="M6 9l6 6 6-6" />,
    close:        <path d="M18 6L6 18M6 6l12 12" />,
    settings:     <><circle cx="12" cy="12" r="3" /><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06A2 2 0 1 1 4.21 16.96l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09a1.65 1.65 0 0 0 1.51-1 1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" /></>,
    mic:          <><rect x="9" y="3" width="6" height="12" rx="3" /><path d="M19 11a7 7 0 0 1-14 0M12 18v3M8 21h8" /></>,
    bell:         <><path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" /><path d="M13.73 21a2 2 0 0 1-3.46 0" /></>,
    bellOff:      <><path d="M13.73 21a2 2 0 0 1-3.46 0M18.63 13A17.89 17.89 0 0 1 18 8M6.26 6.26A5.86 5.86 0 0 0 6 8c0 7-3 9-3 9h14M18 8a6 6 0 0 0-9.33-5M1 1l22 22" /></>,
    play:         <polygon points="5 3 19 12 5 21 5 3" fill="currentColor" stroke="none" />,
    pause:        <><rect x="6" y="4" width="4" height="16" /><rect x="14" y="4" width="4" height="16" /></>,
    watch:        <><rect x="6" y="6" width="12" height="12" rx="3" /><path d="M9 2h6M9 22h6" /></>,
    baby:         <><circle cx="12" cy="10" r="6" /><path d="M8 10h.01M16 10h.01M9 14c1 1 2 1.5 3 1.5s2-.5 3-1.5" /></>,
    waveform:     <path d="M2 12h2l2-6 3 12 3-18 3 18 3-12 2 6h2" />,
    check:        <path d="M20 6L9 17l-5-5" />,
    shield:       <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />,
    history:      <><circle cx="12" cy="12" r="10" /><path d="M12 6v6l4 2" /></>,
    vibrate:      <><path d="M4 8v8M8 6v12M12 4v16M16 6v12M20 8v8" /></>,
    plus:         <><path d="M12 5v14M5 12h14" /></>,
  };
  return <svg {...props}>{paths[name]}</svg>;
};

// ─── 공통: 안드로이드 상태바 + 제스처 바를 화면에 맞춘 색으로 ───
const StatusBar = ({ color, dark }) => {
  const c = dark ? '#fff' : '#171d1b';
  return (
    <div style={{ height: 40, display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 20px', position: 'relative', background: color, flexShrink: 0, fontFamily: 'Roboto, system-ui, sans-serif' }}>
      <span style={{ fontSize: 14, fontWeight: 500, color: c }}>9:30</span>
      <div style={{ position: 'absolute', left: '50%', top: 8, transform: 'translateX(-50%)', width: 24, height: 24, borderRadius: 100, background: '#2e2e2e' }} />
      <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
        <svg width="14" height="14" viewBox="0 0 16 16"><path d="M8 13.3L.67 5.97a10.37 10.37 0 0114.66 0L8 13.3z" fill={c}/></svg>
        <svg width="14" height="14" viewBox="0 0 16 16"><path d="M14.67 14.67V1.33L1.33 14.67h13.34z" fill={c}/></svg>
        <svg width="16" height="16" viewBox="0 0 16 16"><rect x="3.75" y="2" width="8.5" height="13" rx="1.5" fill={c}/><rect x="5.5" y="0.9" width="5" height="2" rx="0.5" fill={c}/></svg>
      </div>
    </div>
  );
};

const NavGesture = ({ color, dark }) => (
  <div style={{ height: 28, background: color, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
    <div style={{ width: 130, height: 4, borderRadius: 2, background: dark ? '#fff' : '#171d1b', opacity: 0.5 }} />
  </div>
);

// ═══════════════════════════════════════════════════════════════════
// 화면 1: 온보딩 — 앱 소개 + 마이크 권한 안내
// ═══════════════════════════════════════════════════════════════════
function OnboardingScreen({ mode = 'light' }) {
  const c = t(mode);
  return (
    <div style={{ width: '100%', height: '100%', background: c.bg, fontFamily: KFONT, color: c.onSurface, display: 'flex', flexDirection: 'column' }}>
      <StatusBar color={c.bg} dark={mode === 'dark'} />

      {/* 메인 콘텐츠 */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', padding: '40px 28px 0' }}>
        {/* 상단 비주얼 — 호흡하는 두 동심원 */}
        <div style={{ height: 240, position: 'relative', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: 36 }}>
          <div style={{ position: 'absolute', width: 220, height: 220, borderRadius: '50%', background: c.primaryFaint, animation: 'breathe-soft 4s ease-in-out infinite' }} />
          <div style={{ position: 'absolute', width: 160, height: 160, borderRadius: '50%', background: c.primary, opacity: 0.35, animation: 'breathe-soft 4s ease-in-out infinite reverse' }} />
          <div style={{ position: 'relative', width: 108, height: 108, borderRadius: '50%', background: c.primary, display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: c.shadowLg }}>
            <Icon name="waveform" size={48} color={c.primaryOn} stroke={2.2} />
          </div>
        </div>

        {/* 카피 */}
        <h1 style={{ fontSize: 30, fontWeight: 700, lineHeight: 1.25, letterSpacing: '-0.02em', margin: 0, marginBottom: 14, textWrap: 'pretty' }}>
          소리를 <span style={{ color: c.primaryDeep }}>진동으로</span><br/>전해 드릴게요
        </h1>
        <p style={{ fontSize: 15.5, lineHeight: 1.55, color: c.onSurfaceMed, margin: 0, marginBottom: 32, textWrap: 'pretty' }}>
          아기의 울음이나 큰 소음이 감지되면<br/>연결된 워치가 손목을 두드려 알려드려요.
        </p>

        {/* 권한 카드들 */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12, marginBottom: 'auto' }}>
          <PermissionRow c={c} icon="mic" title="마이크 접근" desc="주변 소리를 듣고 데시벨을 측정합니다" granted />
          <PermissionRow c={c} icon="watch" title="워치 연결" desc="미밴드 · 갤럭시워치 · 애플워치" granted={false} />
        </div>
      </div>

      {/* 하단 CTA */}
      <div style={{ padding: '20px 28px 24px', display: 'flex', flexDirection: 'column', gap: 12 }}>
        {/* 페이지 인디케이터 */}
        <div style={{ display: 'flex', justifyContent: 'center', gap: 6, marginBottom: 10 }}>
          <Dot active c={c} /><Dot c={c} /><Dot c={c} />
        </div>
        <button style={{
          height: 56, borderRadius: 100, border: 'none', background: c.primary, color: c.primaryOn,
          fontSize: 16, fontWeight: 600, fontFamily: KFONT, letterSpacing: '-0.01em', cursor: 'pointer',
          boxShadow: c.shadow,
        }}>시작하기</button>
        <button style={{
          height: 44, borderRadius: 100, border: 'none', background: 'transparent', color: c.onSurfaceMed,
          fontSize: 14, fontFamily: KFONT, cursor: 'pointer',
        }}>이미 계정이 있어요</button>
      </div>

      <NavGesture color={c.bg} dark={mode === 'dark'} />
    </div>
  );
}

const Dot = ({ active, c }) => (
  <div style={{ width: active ? 24 : 6, height: 6, borderRadius: 3, background: active ? c.primary : c.border, transition: 'all .2s' }} />
);

const PermissionRow = ({ c, icon, title, desc, granted }) => (
  <div style={{ display: 'flex', alignItems: 'center', gap: 14, padding: '14px 16px', background: c.surface, borderRadius: 20, border: `1px solid ${c.border}` }}>
    <div style={{ width: 44, height: 44, borderRadius: 14, background: c.primaryFaint, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
      <Icon name={icon} size={22} color={c.primaryDeep} />
    </div>
    <div style={{ flex: 1, minWidth: 0 }}>
      <div style={{ fontSize: 15, fontWeight: 600, marginBottom: 2 }}>{title}</div>
      <div style={{ fontSize: 13, color: c.onSurfaceMed, lineHeight: 1.3 }}>{desc}</div>
    </div>
    {granted ? (
      <div style={{ width: 28, height: 28, borderRadius: 14, background: c.primary, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
        <Icon name="check" size={16} color={c.primaryOn} stroke={3} />
      </div>
    ) : (
      <div style={{ fontSize: 13, fontWeight: 600, color: c.primaryDeep, padding: '6px 12px', borderRadius: 100, background: c.primaryFaint, flexShrink: 0 }}>연결</div>
    )}
  </div>
);

// ═══════════════════════════════════════════════════════════════════
// 화면 2: 메인 — 실시간 데시벨 + 호흡하는 원
// ═══════════════════════════════════════════════════════════════════
function MainScreen({ mode = 'light', threshold = 65, simDb = null }) {
  const c = t(mode);
  // dB 시뮬레이션
  const [db, setDb] = React.useState(simDb !== null ? simDb : 38);
  React.useEffect(() => {
    if (simDb !== null) { setDb(simDb); return; }
    let raf;
    const loop = () => {
      setDb(prev => {
        const target = 32 + Math.sin(Date.now() / 1900) * 10 + Math.random() * 6;
        return prev + (target - prev) * 0.15;
      });
      raf = requestAnimationFrame(loop);
    };
    raf = requestAnimationFrame(loop);
    return () => cancelAnimationFrame(raf);
  }, [simDb]);

  const isAlert = db >= threshold;
  const isWarn = db >= threshold - 12 && !isAlert;
  // 원 크기: 0dB→0.35배, 100dB→1.0배
  const circleScale = Math.max(0.4, Math.min(1.05, 0.4 + (db / 100) * 0.7));
  const circleColor = isAlert ? c.alert : (isWarn ? c.warn : c.primary);
  const circleFaint = isAlert ? c.alertFaint : (isWarn ? c.warnFaint : c.primaryFaint);
  const bgTint = isAlert ? c.alertFaint : c.bg;

  return (
    <div style={{ width: '100%', height: '100%', background: bgTint, fontFamily: KFONT, color: c.onSurface, display: 'flex', flexDirection: 'column', transition: 'background .3s', position: 'relative', overflow: 'hidden' }}>
      {/* 알림 시 화면 펄스 — 시각 진동 */}
      {isAlert && <div style={{ position: 'absolute', inset: 0, pointerEvents: 'none', boxShadow: `inset 0 0 0 6px ${c.alert}`, animation: 'screen-pulse 0.7s ease-in-out infinite' }} />}

      <StatusBar color="transparent" dark={mode === 'dark'} />

      {/* 앱바 */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '8px 12px 8px 20px', flexShrink: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <div style={{ width: 36, height: 36, borderRadius: 12, background: c.primaryFaint, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Icon name="baby" size={20} color={c.primaryDeep} />
          </div>
          <div>
            <div style={{ fontSize: 15, fontWeight: 600, lineHeight: 1.1 }}>아기방</div>
            <div style={{ fontSize: 11.5, color: c.onSurfaceMed, marginTop: 1, display: 'flex', alignItems: 'center', gap: 4 }}>
              <span style={{ width: 6, height: 6, borderRadius: 3, background: c.primary, display: 'inline-block' }} />
              감지 중 · 미밴드 연결됨
            </div>
          </div>
        </div>
        <button style={iconBtn(c)}><Icon name="settings" size={20} color={c.onSurfaceMed} /></button>
      </div>

      {/* 호흡하는 원 */}
      <div style={{ flex: 1, position: 'relative', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '8px 0' }}>
        {/* 외곽 임계값 링 */}
        <div style={{
          position: 'absolute', width: 280, height: 280, borderRadius: '50%',
          border: `1.5px dashed ${c.border}`, opacity: 0.8,
        }} />
        <div style={{
          position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%) translate(0, -154px)',
          background: c.surface, padding: '4px 10px', borderRadius: 100, border: `1px solid ${c.border}`,
          fontSize: 11, fontWeight: 600, color: c.onSurfaceMed,
        }}>임계값 {threshold}dB</div>

        {/* 가장 바깥 부드러운 원 */}
        <div style={{
          position: 'absolute', width: 260, height: 260, borderRadius: '50%',
          background: circleFaint, opacity: 0.7, transform: `scale(${circleScale * 0.95})`,
          transition: 'transform .2s, background .3s', animation: isAlert ? 'breathe-fast 0.8s ease-in-out infinite' : 'breathe-soft 4s ease-in-out infinite',
        }} />
        {/* 중간 원 */}
        <div style={{
          position: 'absolute', width: 200, height: 200, borderRadius: '50%',
          background: circleColor, opacity: isAlert ? 0.45 : 0.25, transform: `scale(${circleScale})`,
          transition: 'transform .15s, background .3s, opacity .3s',
          animation: isAlert ? 'breathe-fast 0.8s ease-in-out infinite reverse' : 'breathe-soft 4s ease-in-out infinite reverse',
        }} />
        {/* 중심 원 — dB 표시 */}
        <div style={{
          position: 'relative', width: 156, height: 156, borderRadius: '50%',
          background: circleColor, display: 'flex', flexDirection: 'column',
          alignItems: 'center', justifyContent: 'center', color: c.primaryOn,
          transform: `scale(${0.85 + circleScale * 0.15})`, transition: 'transform .15s, background .3s',
          boxShadow: isAlert ? `0 0 40px ${c.alert}55` : c.shadowLg,
        }}>
          <div style={{ fontSize: 11, fontWeight: 600, opacity: 0.85, letterSpacing: 1, marginBottom: -2 }}>현재 소음</div>
          <div style={{ fontSize: 56, fontWeight: 700, fontVariantNumeric: 'tabular-nums', lineHeight: 1, letterSpacing: '-0.04em' }}>{Math.round(db)}</div>
          <div style={{ fontSize: 13, fontWeight: 600, opacity: 0.9, marginTop: 2 }}>dB</div>
        </div>
      </div>

      {/* 상태 칩 */}
      <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 14 }}>
        <StatusChip c={c} isAlert={isAlert} isWarn={isWarn} db={db} threshold={threshold} />
      </div>

      {/* 하단: 최근 감지 + 컨트롤 */}
      <div style={{ background: c.surface, borderRadius: '28px 28px 0 0', padding: '22px 24px 18px', boxShadow: c.shadowLg, flexShrink: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 14 }}>
          <div style={{ fontSize: 14, fontWeight: 600, color: c.onSurface }}>최근 감지</div>
          <div style={{ fontSize: 12, color: c.onSurfaceMed }}>오늘 4회</div>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginBottom: 18 }}>
          <DetectionRow c={c} time="9:12" desc="아기 울음 추정" db={82} kind="alert" />
          <DetectionRow c={c} time="8:34" desc="높은 소음" db={71} kind="warn" />
        </div>
        <button style={{ ...primaryBtn(c), width: '100%' }}>
          <Icon name="pause" size={18} color={c.primaryOn} />일시정지
        </button>
      </div>

      <NavGesture color={c.surface} dark={mode === 'dark'} />
    </div>
  );
}

const StatusChip = ({ c, isAlert, isWarn, db, threshold }) => {
  const [bg, fg, label] = isAlert
    ? [c.alert, c.primaryOn, `임계값 초과 · 워치 진동 중`]
    : isWarn
      ? [c.warnFaint, c.warn, `주의 · 임계값 ${threshold - Math.round(db)}dB 남음`]
      : [c.primaryFaint, c.primaryDeep, '조용한 환경 · 정상 감지'];
  return (
    <div style={{ display: 'inline-flex', alignItems: 'center', gap: 8, padding: '8px 16px', borderRadius: 100, background: bg, color: fg, fontSize: 13, fontWeight: 600 }}>
      <span style={{ width: 6, height: 6, borderRadius: 3, background: 'currentColor', animation: isAlert ? 'blink 0.5s infinite' : 'none' }} />
      {label}
    </div>
  );
};

const DetectionRow = ({ c, time, desc, db, kind }) => {
  const accent = kind === 'alert' ? c.alert : c.warn;
  const faint = kind === 'alert' ? c.alertFaint : c.warnFaint;
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '10px 12px', background: c.surfaceSunken, borderRadius: 16 }}>
      <div style={{ width: 36, height: 36, borderRadius: 12, background: faint, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
        <Icon name={kind === 'alert' ? 'baby' : 'waveform'} size={18} color={accent} />
      </div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 14, fontWeight: 500, color: c.onSurface }}>{desc}</div>
        <div style={{ fontSize: 12, color: c.onSurfaceMed, marginTop: 1 }}>오늘 {time}</div>
      </div>
      <div style={{ fontSize: 14, fontWeight: 700, color: accent, fontVariantNumeric: 'tabular-nums' }}>{db}<span style={{ fontSize: 10, fontWeight: 600, marginLeft: 1 }}>dB</span></div>
    </div>
  );
};

// ═══════════════════════════════════════════════════════════════════
// 화면 3: 민감도/임계값 설정
// ═══════════════════════════════════════════════════════════════════
function SettingsScreen({ mode = 'light', threshold = 65, setThreshold, onTestAlert }) {
  const c = t(mode);
  const [pulsing, setPulsing] = React.useState(false);

  // 슬라이더 — 30~100dB 매핑
  const min = 30, max = 100;
  const pct = ((threshold - min) / (max - min)) * 100;

  const handleTest = () => {
    setPulsing(true);
    setTimeout(() => setPulsing(false), 1600);
    onTestAlert && onTestAlert();
  };

  return (
    <div style={{ width: '100%', height: '100%', background: c.bg, fontFamily: KFONT, color: c.onSurface, display: 'flex', flexDirection: 'column', position: 'relative', overflow: 'hidden' }}>
      {pulsing && <div style={{ position: 'absolute', inset: 0, pointerEvents: 'none', boxShadow: `inset 0 0 0 6px ${c.alert}`, animation: 'screen-pulse 0.55s ease-in-out 3', zIndex: 5 }} />}

      <StatusBar color={c.bg} dark={mode === 'dark'} />

      {/* 앱바 */}
      <div style={{ display: 'flex', alignItems: 'center', padding: '8px 12px 4px', flexShrink: 0 }}>
        <button style={iconBtn(c)}><Icon name="chevronLeft" size={22} color={c.onSurface} /></button>
        <div style={{ flex: 1, fontSize: 17, fontWeight: 600, marginLeft: 4 }}>민감도 설정</div>
        <button style={iconBtn(c)}><Icon name="close" size={20} color={c.onSurfaceMed} /></button>
      </div>

      <div style={{ flex: 1, overflow: 'auto', padding: '8px 20px 16px' }}>
        {/* 임계값 큰 수치 */}
        <div style={{ background: c.surface, borderRadius: 24, padding: '22px 22px 18px', marginBottom: 14, border: `1px solid ${c.border}` }}>
          <div style={{ fontSize: 12, fontWeight: 600, color: c.onSurfaceMed, letterSpacing: 0.4, marginBottom: 4 }}>알림 임계값</div>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: 6, marginBottom: 18 }}>
            <div style={{ fontSize: 56, fontWeight: 700, color: c.primaryDeep, lineHeight: 1, letterSpacing: '-0.04em', fontVariantNumeric: 'tabular-nums' }}>{threshold}</div>
            <div style={{ fontSize: 18, fontWeight: 600, color: c.primaryDeep }}>dB</div>
            <div style={{ marginLeft: 'auto', fontSize: 12, color: c.onSurfaceMed, fontWeight: 500 }}>이 값 이상이면<br/>워치가 진동해요</div>
          </div>

          {/* 슬라이더 */}
          <div style={{ position: 'relative', height: 36, marginBottom: 6 }}>
            <div style={{ position: 'absolute', top: '50%', left: 0, right: 0, height: 8, borderRadius: 4, background: c.surfaceSunken, transform: 'translateY(-50%)' }}>
              <div style={{ position: 'absolute', top: 0, left: 0, height: '100%', width: `${pct}%`, background: `linear-gradient(90deg, ${c.primary}, ${c.warn} 70%, ${c.alert})`, borderRadius: 4 }} />
            </div>
            <input
              type="range" min={min} max={max} value={threshold}
              onChange={(e) => setThreshold && setThreshold(parseInt(e.target.value))}
              style={{
                position: 'absolute', inset: 0, width: '100%', appearance: 'none',
                background: 'transparent', cursor: 'pointer', WebkitAppearance: 'none',
              }}
              className="db-slider"
            />
            <div style={{ position: 'absolute', top: '50%', left: `${pct}%`, transform: 'translate(-50%, -50%)', width: 28, height: 28, borderRadius: 14, background: c.surface, border: `3px solid ${c.primaryDeep}`, boxShadow: c.shadow, pointerEvents: 'none' }} />
          </div>

          {/* 눈금 + 라벨 */}
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 10.5, color: c.onSurfaceDim, fontWeight: 500, marginTop: 4 }}>
            <span>30</span><span>50</span><span>70</span><span>100</span>
          </div>
          {/* 참고선 */}
          <div style={{ marginTop: 14, display: 'flex', flexDirection: 'column', gap: 6 }}>
            <RefRow c={c} label="속삭임" db="30" />
            <RefRow c={c} label="일반 대화" db="60" />
            <RefRow c={c} label="아기 울음" db="80" highlight />
          </div>
        </div>

        {/* 알림 테스트 */}
        <button onClick={handleTest} style={{
          width: '100%', padding: '18px 20px', borderRadius: 20, background: c.surface,
          border: `1px solid ${c.border}`, display: 'flex', alignItems: 'center', gap: 14,
          fontFamily: KFONT, cursor: 'pointer', textAlign: 'left',
        }}>
          <div style={{ width: 44, height: 44, borderRadius: 14, background: c.alertFaint, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Icon name="bell" size={22} color={c.alert} />
          </div>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 15, fontWeight: 600, color: c.onSurface }}>알림 미리보기</div>
            <div style={{ fontSize: 12.5, color: c.onSurfaceMed, marginTop: 2 }}>화면 펄스 + 워치 진동을 테스트해요</div>
          </div>
          <div style={{ fontSize: 13, fontWeight: 600, color: c.primaryDeep, padding: '8px 14px', borderRadius: 100, background: c.primaryFaint }}>실행</div>
        </button>
      </div>

      <NavGesture color={c.bg} dark={mode === 'dark'} />
    </div>
  );
}

const RefRow = ({ c, label, db, highlight }) => (
  <div style={{ display: 'flex', alignItems: 'center', gap: 10, fontSize: 12.5 }}>
    <div style={{ width: 32, fontVariantNumeric: 'tabular-nums', fontWeight: 700, color: highlight ? c.alert : c.onSurfaceMed }}>{db}dB</div>
    <div style={{ flex: 1, height: 2, background: c.surfaceSunken, borderRadius: 1, position: 'relative' }}>
      <div style={{ position: 'absolute', left: 0, top: 0, height: '100%', width: `${(parseInt(db) - 30) / 70 * 100}%`, background: highlight ? c.alert : c.onSurfaceDim, borderRadius: 1 }} />
    </div>
    <div style={{ color: highlight ? c.alert : c.onSurfaceMed, fontWeight: highlight ? 600 : 500 }}>{label}</div>
  </div>
);

const SettingCard = ({ c, title, subtitle, children }) => (
  <div style={{ background: c.surface, borderRadius: 24, padding: 20, marginBottom: 14, border: `1px solid ${c.border}` }}>
    <div style={{ marginBottom: 14 }}>
      <div style={{ fontSize: 15, fontWeight: 600 }}>{title}</div>
      {subtitle && <div style={{ fontSize: 12.5, color: c.onSurfaceMed, marginTop: 2 }}>{subtitle}</div>}
    </div>
    {children}
  </div>
);

const SegmentedControl = ({ c, value, onChange, options }) => (
  <div style={{ display: 'flex', background: c.surfaceSunken, borderRadius: 100, padding: 4, gap: 2 }}>
    {options.map(o => {
      const active = value === o.v;
      return (
        <button key={o.v} onClick={() => onChange && onChange(o.v)} style={{
          flex: 1, padding: '10px 0', borderRadius: 100, border: 'none',
          background: active ? c.surface : 'transparent', color: active ? c.primaryDeep : c.onSurfaceMed,
          fontSize: 13, fontWeight: 600, fontFamily: KFONT, cursor: 'pointer',
          boxShadow: active ? c.shadow : 'none', transition: 'all .15s',
        }}>{o.label}</button>
      );
    })}
  </div>
);

// ─── 공통 버튼 스타일 ───
const iconBtn = (c) => ({ width: 40, height: 40, borderRadius: 20, border: 'none', background: 'transparent', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' });
const iconBtnLg = (c) => ({ width: 56, height: 56, borderRadius: 18, border: `1px solid ${c.border}`, background: c.surfaceSunken, display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' });
const primaryBtn = (c) => ({ height: 56, borderRadius: 18, border: 'none', background: c.primary, color: c.primaryOn, fontSize: 15, fontWeight: 600, fontFamily: KFONT, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8, cursor: 'pointer' });

// ═══════════════════════════════════════════════════════════════════
// 앱 아이콘 — Adaptive Icon (108×108dp, 안전 영역 66×66dp 중앙)
// ═══════════════════════════════════════════════════════════════════
// Android 적응형 아이콘은 마스크가 다양 (원/사각/스쿼클 등). 콘텐츠는
// 중앙 66×66dp 안에만 들어가야 안전. 여기서는 432×432 캔버스로 그려서
// 어떤 사이즈로도 다운스케일이 가능하게 한다.

// 깔끔한 하트 path — Material 3 톤, 단일 글리프 (중심 216,216)
const HEART_432 = 'M216,298 C150,250 118,212 118,172 C118,142 142,120 170,120 C191,120 208,132 216,151 C224,132 241,120 262,120 C290,120 314,142 314,172 C314,212 282,250 216,298 Z';

function AppIcon({ size = 192, mask = 'squircle' }) {
  // Material 3 — 단색 배경 + 중앙 하트 하나. "아이고 내새끼"의 애정.
  const id = React.useId().replace(/:/g, '');
  const clipPath = {
    squircle: 'M 216,0 Q 432,0 432,216 Q 432,432 216,432 Q 0,432 0,216 Q 0,0 216,0 Z',
    circle: 'M 216,0 A 216,216 0 1 1 215.99,0 Z',
    round: 'M 108,0 L 324,0 Q 432,0 432,108 L 432,324 Q 432,432 324,432 L 108,432 Q 0,432 0,324 L 0,108 Q 0,0 108,0 Z',
  }[mask] || 'M 216,0 Q 432,0 432,216 Q 432,432 216,432 Q 0,432 0,216 Q 0,0 216,0 Z';

  return (
    <svg width={size} height={size} viewBox="0 0 432 432" style={{ display: 'block' }}>
      <defs>
        <clipPath id={`clip-${id}`}>
          <path d={clipPath} />
        </clipPath>
      </defs>
      <g clipPath={`url(#clip-${id})`}>
        {/* 단색 배경 — 세이지 그린 (안전·안정) */}
        <rect width="432" height="432" fill="#5C9989" />
        {/* 중앙 하트 */}
        <path d={HEART_432} fill="#FFFFFF" />
      </g>
    </svg>
  );
}

// 단색(monochrome) 아이콘 — 안드로이드 13+ 테마 아이콘용
function AppIconMono({ size = 192, color = '#3F7868' }) {
  return (
    <svg width={size} height={size} viewBox="0 0 432 432" style={{ display: 'block' }}>
      <path d={HEART_432} fill={color} />
    </svg>
  );
}

// ═══════════════════════════════════════════════════════════════════
// 화면 0: 런치 / 스플래시 — Android 12+ Splash Screen API 기반
// ═══════════════════════════════════════════════════════════════════
function LaunchScreen({ mode = 'light' }) {
  return (
    <div style={{ width: '100%', height: '100%', background: '#FFFFFF', fontFamily: KFONT, display: 'flex', flexDirection: 'column', position: 'relative' }}>
      <StatusBar color="transparent" dark={false} />

      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 14 }}>
        <div style={{ fontSize: 34, fontWeight: 700, color: '#3F7868', letterSpacing: '-0.02em' }}>
          I go my baby
        </div>
        <div style={{ fontSize: 17, fontWeight: 500, color: '#1f1d1a', letterSpacing: '-0.005em', whiteSpace: 'nowrap' }}>
          - 아이고 내새끼 -
        </div>
      </div>

      <NavGesture color="transparent" dark={false} />
    </div>
  );
}

// ═══════════════════════════════════════════════════════════════════
// 아이콘 갤러리 — 핸드오프용 (다양한 마스크 + 모노 + 사이즈)
// ═══════════════════════════════════════════════════════════════════
function IconShowcase({ mode = 'light' }) {
  const c = t(mode);
  return (
    <div style={{ width: '100%', height: '100%', background: c.bg, fontFamily: KFONT, color: c.onSurface, padding: '40px 28px', display: 'flex', flexDirection: 'column', overflow: 'auto' }}>
      <div style={{ marginBottom: 28 }}>
        <div style={{ fontSize: 11, fontWeight: 600, color: c.onSurfaceMed, letterSpacing: 1.2 }}>APP ICON</div>
        <div style={{ fontSize: 22, fontWeight: 700, marginTop: 4, letterSpacing: '-0.02em' }}>아이고 내새끼 · I Go My Baby</div>
        <div style={{ fontSize: 12.5, color: c.onSurfaceMed, marginTop: 4, lineHeight: 1.4 }}>Material 3 · 세이지 그린 + 하트 글리프</div>
      </div>

      {/* 마스크별 미리보기 — Android는 런처마다 다른 마스크 */}
      <div style={{ marginBottom: 28 }}>
        <div style={{ fontSize: 13, fontWeight: 600, color: c.onSurfaceMed, marginBottom: 14 }}>적응형 아이콘 — 마스크별</div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 14 }}>
          <MaskCard c={c} mask="circle" label="원형" />
          <MaskCard c={c} mask="squircle" label="스쿼클" />
          <MaskCard c={c} mask="round" label="라운드 사각" />
        </div>
      </div>

      {/* 사이즈별 — 작은 사이즈에서도 가독성 */}
      <div style={{ marginBottom: 28 }}>
        <div style={{ fontSize: 13, fontWeight: 600, color: c.onSurfaceMed, marginBottom: 14 }}>사이즈 스케일</div>
        <div style={{ display: 'flex', alignItems: 'flex-end', gap: 18, padding: '20px 18px', background: c.surface, borderRadius: 20, border: `1px solid ${c.border}` }}>
          {[
            { s: 96, l: '96dp' },
            { s: 72, l: '72dp' },
            { s: 48, l: '48dp · 알림' },
            { s: 32, l: '32dp · 작게' },
            { s: 24, l: '24dp' },
          ].map((x) => (
            <div key={x.s} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 6 }}>
              <div style={{ width: x.s, height: x.s, borderRadius: x.s * 0.22, overflow: 'hidden' }}>
                <AppIcon size={x.s} mask="squircle" />
              </div>
              <div style={{ fontSize: 9.5, color: c.onSurfaceDim, fontWeight: 500 }}>{x.l}</div>
            </div>
          ))}
        </div>
      </div>

      {/* 모노 아이콘 (Android 13+ 테마) + 알림 아이콘 */}
      <div>
        <div style={{ fontSize: 13, fontWeight: 600, color: c.onSurfaceMed, marginBottom: 14 }}>모노크롬 — 테마 아이콘 / 알림</div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 14 }}>
          <div style={{ background: '#1f1d1a', borderRadius: 20, padding: 18, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 10 }}>
            <div style={{ width: 72, height: 72, borderRadius: 18, background: '#FFFCF7', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <AppIconMono size={56} color="#1f1d1a" />
            </div>
            <div style={{ fontSize: 11, color: '#C8C0B0', fontWeight: 500 }}>다크 런처 테마</div>
          </div>
          <div style={{ background: c.surfaceSunken, borderRadius: 20, padding: 18, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 10 }}>
            <div style={{ width: 72, height: 72, borderRadius: 18, background: '#5C9989', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <AppIconMono size={56} color="#FFFCF7" />
            </div>
            <div style={{ fontSize: 11, color: c.onSurfaceMed, fontWeight: 500 }}>알림 아이콘 (24dp)</div>
          </div>
        </div>
      </div>
    </div>
  );
}

function MaskCard({ c, mask, label }) {
  return (
    <div style={{ background: c.surface, borderRadius: 20, padding: 18, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 10, border: `1px solid ${c.border}` }}>
      <AppIcon size={84} mask={mask} />
      <div style={{ fontSize: 11.5, color: c.onSurfaceMed, fontWeight: 500 }}>{label}</div>
    </div>
  );
}

Object.assign(window, { OnboardingScreen, MainScreen, SettingsScreen, LaunchScreen, IconShowcase, AppIcon, AppIconMono, t, KFONT, Icon });
