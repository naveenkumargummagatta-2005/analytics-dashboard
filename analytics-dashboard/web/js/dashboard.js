// ============================================================
// Dashboard frontend logic.
// Fetches the JSON produced by the Java pipeline and renders it
// with Chart.js. Charts are created once and updated in place
// on refresh, rather than destroyed/recreated, to keep things
// snappy and avoid flicker.
// ============================================================

const API_DATA_URL = '/api/data';
const API_REFRESH_URL = '/api/refresh';

const charts = {}; // keyed by canvas id, holds Chart.js instances

const CHART_COLORS = ['#3ddc97', '#5aa9ff', '#ffb454', '#ff6b81', '#b48bff', '#4dd9ff', '#ff9f5a', '#7ee787'];

document.addEventListener('DOMContentLoaded', () => {
  loadDashboard();
  document.getElementById('refreshBtn').addEventListener('click', refreshPipeline);
});

async function loadDashboard() {
  setStatus('loading', 'loading dashboard-data.json…');
  try {
    const res = await fetch(API_DATA_URL, { cache: 'no-store' });
    if (!res.ok) {
      throw new Error(`Server responded ${res.status}`);
    }
    const data = await res.json();
    renderKpis(data);
    renderCharts(data);
    setStatus('ok', 'pipeline data loaded');
  } catch (err) {
    console.error(err);
    setStatus('error', 'could not load data — is the pipeline run yet?');
  }
}

async function refreshPipeline() {
  const btn = document.getElementById('refreshBtn');
  btn.disabled = true;
  btn.textContent = 'Running…';
  setStatus('loading', 're-running Java pipeline…');
  try {
    const res = await fetch(API_REFRESH_URL, { cache: 'no-store' });
    if (!res.ok) throw new Error(`Refresh failed (${res.status})`);
    await loadDashboard();
  } catch (err) {
    console.error(err);
    setStatus('error', 'pipeline refresh failed — check server console');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Re-run pipeline';
  }
}

function setStatus(state, text) {
  const el = document.getElementById('statusIndicator');
  el.classList.remove('status--ok', 'status--error');
  if (state === 'ok') el.classList.add('status--ok');
  if (state === 'error') el.classList.add('status--error');
  el.querySelector('.status__text').textContent = text;
}

function renderKpis(data) {
  document.getElementById('kpiRevenue').textContent = formatCurrency(data.totalRevenue);
  document.getElementById('kpiRecords').textContent = data.totalRecords;
  document.getElementById('kpiCategories').textContent = Object.keys(data.revenueByCategory).length;
  document.getElementById('kpiMonths').textContent = Object.keys(data.revenueByMonth).length;
}

function renderCharts(data) {
  renderTrendChart(data.revenueByMonth);
  renderCategoryChart(data.revenueByCategory);
  renderRegionChart(data.revenueByRegion);
  renderProductChart(data.topProducts);
}

function renderTrendChart(revenueByMonth) {
  const labels = Object.keys(revenueByMonth);
  const values = Object.values(revenueByMonth);
  upsertChart('trendChart', 'line', {
    labels,
    datasets: [{
      label: 'Revenue',
      data: values,
      borderColor: '#3ddc97',
      backgroundColor: 'rgba(61, 220, 151, 0.12)',
      fill: true,
      tension: 0.35,
      pointRadius: 3,
      pointBackgroundColor: '#3ddc97',
    }]
  }, { plugins: { legend: { display: false } } });
}

function renderCategoryChart(revenueByCategory) {
  const labels = Object.keys(revenueByCategory);
  const values = Object.values(revenueByCategory);
  upsertChart('categoryChart', 'doughnut', {
    labels,
    datasets: [{
      data: values,
      backgroundColor: CHART_COLORS,
      borderColor: '#151b23',
      borderWidth: 2,
    }]
  }, { plugins: { legend: { position: 'bottom', labels: { color: '#8b96a5', font: { family: 'IBM Plex Mono', size: 11 } } } } });
}

function renderRegionChart(revenueByRegion) {
  const labels = Object.keys(revenueByRegion);
  const values = Object.values(revenueByRegion);
  upsertChart('regionChart', 'bar', {
    labels,
    datasets: [{
      label: 'Revenue',
      data: values,
      backgroundColor: '#5aa9ff',
      borderRadius: 4,
      maxBarThickness: 48,
    }]
  }, { plugins: { legend: { display: false } } });
}

function renderProductChart(topProducts) {
  upsertChart('productChart', 'bar', {
    labels: topProducts.labels,
    datasets: [{
      label: 'Units sold',
      data: topProducts.units,
      backgroundColor: '#ffb454',
      borderRadius: 4,
    }]
  }, {
    indexAxis: 'y',
    plugins: { legend: { display: false } }
  });
}

/** Creates a chart on first call, updates data in place on later calls. */
function upsertChart(canvasId, type, data, extraOptions = {}) {
  const ctx = document.getElementById(canvasId).getContext('2d');

  const baseOptions = {
    responsive: true,
    maintainAspectRatio: false,
    scales: (type === 'doughnut') ? {} : {
      x: {
        ticks: { color: '#8b96a5', font: { family: 'IBM Plex Mono', size: 11 } },
        grid: { color: 'rgba(255,255,255,0.05)' }
      },
      y: {
        ticks: { color: '#8b96a5', font: { family: 'IBM Plex Mono', size: 11 } },
        grid: { color: 'rgba(255,255,255,0.05)' }
      }
    }
  };

  const options = deepMerge(baseOptions, extraOptions);

  if (charts[canvasId]) {
    charts[canvasId].data = data;
    charts[canvasId].options = options;
    charts[canvasId].update();
  } else {
    charts[canvasId] = new Chart(ctx, { type, data, options });
  }
}

function deepMerge(target, source) {
  const result = { ...target };
  for (const key of Object.keys(source)) {
    if (source[key] && typeof source[key] === 'object' && !Array.isArray(source[key])) {
      result[key] = deepMerge(target[key] || {}, source[key]);
    } else {
      result[key] = source[key];
    }
  }
  return result;
}

function formatCurrency(amount) {
  return '$' + Number(amount).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
