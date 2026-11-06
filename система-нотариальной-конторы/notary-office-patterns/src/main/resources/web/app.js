const state = {
    token: null,
    role: null,
    client: null,
    services: [],
    applications: [],
    page: 'dashboard',
    loginRole: 'client'
};

const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => [...document.querySelectorAll(selector)];
const money = new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', maximumFractionDigits: 2 });
const pageNames = {
    dashboard: ['Рабочее пространство', 'Главная'],
    applications: ['Заявки и документы', 'Заявки'],
    services: ['Нотариальные услуги', 'Каталог услуг'],
    notifications: ['События по заявкам', 'Уведомления'],
    profile: ['Личные данные', 'Профиль'],
    audit: ['Контроль операций', 'Журнал аудита']
};

function esc(value) {
    return String(value ?? '').replace(/[&<>'"]/g, char => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;'
    })[char]);
}

function formatDate(value) {
    if (!value) return '—';
    return new Date(value).toLocaleString('ru-RU', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

function statusBadge(status, label) {
    return `<span class="status status-${String(status).toLowerCase()}">${esc(label || status)}</span>`;
}

function servicePrice(service) {
    return service.pricingType === 'PERCENTAGE' ? '0,5% от сделки' : money.format(service.price);
}

async function api(path, options = {}) {
    const headers = { ...(options.headers || {}) };
    if (state.token) headers.Authorization = `Bearer ${state.token}`;
    if (options.body && typeof options.body !== 'string') {
        headers['Content-Type'] = 'application/json';
        options.body = JSON.stringify(options.body);
    }
    const response = await fetch(path, { ...options, headers });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        if (response.status === 401 && !path.includes('/login')) {
            clearSession();
            showAuth();
        }
        throw new Error(data.error || `Ошибка запроса (${response.status})`);
    }
    return data;
}

function toast(message, error = false) {
    const element = document.createElement('div');
    element.className = `toast${error ? ' error' : ''}`;
    element.textContent = message;
    $('#toasts').append(element);
    setTimeout(() => element.remove(), 4500);
}

function saveSession() {
    localStorage.setItem('notarySession', JSON.stringify({ token: state.token, role: state.role, client: state.client }));
}

function clearSession() {
    localStorage.removeItem('notarySession');
    state.token = null;
    state.role = null;
    state.client = null;
}

function showAuth() {
    $('#app-view').hidden = true;
    $('#auth-view').hidden = false;
}

function switchAuthTab(tab) {
    $$('[data-auth-tab]').forEach(button => button.classList.toggle('active', button.dataset.authTab === tab));
    $('#login-form').hidden = tab !== 'login';
    $('#register-form').hidden = tab !== 'register';
    $('#auth-title').textContent = tab === 'login' ? 'С возвращением' : 'Создайте кабинет';
    $('#auth-subtitle').textContent = tab === 'login'
        ? 'Войдите, чтобы продолжить работу с документами'
        : 'Заполните данные для регистрации клиента';
}

async function showApp() {
    $('#auth-view').hidden = true;
    $('#app-view').hidden = false;
    const isClient = state.role === 'CLIENT';
    $('#workspace-label').textContent = isClient ? 'Кабинет клиента' : 'Рабочее место нотариуса';
    $('#sidebar-name').textContent = isClient ? state.client.fullName : 'Нотариус';
    $('#sidebar-email').textContent = isClient ? state.client.email : 'Сотрудник конторы';
    $('#sidebar-avatar').textContent = isClient ? state.client.fullName.trim().charAt(0).toUpperCase() : 'Н';
    $('.new-application-top').hidden = !isClient;
    renderNavigation();
    await navigate('dashboard');
}

function renderNavigation() {
    const items = state.role === 'CLIENT'
        ? [
            ['dashboard', '▦', 'Главная'], ['applications', '▤', 'Мои заявки'],
            ['services', '◇', 'Каталог услуг'], ['notifications', '♢', 'Уведомления'],
            ['profile', '○', 'Мой профиль']
        ]
        : [
            ['dashboard', '▦', 'Главная'], ['applications', '▤', 'Все заявки'],
            ['services', '◇', 'Управление услугами'], ['audit', '≡', 'Журнал аудита']
        ];
    $('#main-nav').innerHTML = items.map(([page, icon, label]) =>
        `<button class="nav-item${state.page === page ? ' active' : ''}" data-page="${page}"><span class="nav-icon">${icon}</span>${label}</button>`
    ).join('');
    $$('[data-page]').forEach(button => button.addEventListener('click', () => navigate(button.dataset.page)));
}

async function navigate(page) {
    state.page = page;
    const [caption, title] = pageNames[page];
    $('#page-caption').textContent = caption;
    $('#page-title').textContent = title;
    $('#page-content').innerHTML = '<div class="loading">Загрузка данных…</div>';
    $('.sidebar').classList.remove('open');
    renderNavigation();
    try {
        if (page === 'dashboard') await renderDashboard();
        if (page === 'applications') await renderApplications();
        if (page === 'services') await renderServices();
        if (page === 'notifications') await renderNotifications();
        if (page === 'profile') renderProfile();
        if (page === 'audit') await renderAudit();
    } catch (error) {
        $('#page-content').innerHTML = emptyState('Не удалось загрузить данные', error.message, '!');
        toast(error.message, true);
    }
}

async function loadCatalog() {
    state.services = await api('/api/services');
}

async function loadApplications() {
    state.applications = await api('/api/applications');
    state.applications.sort((a, b) => b.id - a.id);
}

async function renderDashboard() {
    await Promise.all([loadCatalog(), loadApplications()]);
    const apps = state.applications;
    const active = apps.filter(app => ['CREATED', 'IN_PROGRESS', 'READY'].includes(app.status)).length;
    const ready = apps.filter(app => app.status === 'READY').length;
    const done = apps.filter(app => app.status === 'RECEIVED').length;
    const isClient = state.role === 'CLIENT';
    const firstName = isClient ? state.client.fullName.split(' ')[1] || state.client.fullName.split(' ')[0] : 'Нотариус';
    const stats = [
        [apps.length, isClient ? 'Всего заявок' : 'Заявок в системе', 'Вся история обращений', '▤'],
        [active, 'Активные', 'Требуют продолжения работы', '◷'],
        [ready, 'Готовы к выдаче', 'Документы подготовлены', '✓'],
        [done, 'Завершено', 'Документы получены', '◇']
    ];
    $('#page-content').innerHTML = `
        <section class="welcome-card">
            <span class="eyebrow light">${isClient ? 'Добро пожаловать' : 'Рабочее место'}</span>
            <h3>${isClient ? `Здравствуйте, ${esc(firstName)}!` : 'Заявки под контролем'}</h3>
            <p>${isClient ? 'Оформите новую заявку или проверьте статус текущих. Мы сообщим, когда документы будут готовы.' : 'Проверяйте обращения клиентов, управляйте статусами и формируйте готовые нотариальные документы.'}</p>
            <button class="button primary" data-action="${isClient ? 'new-application' : 'go-applications'}">${isClient ? '+ Создать заявку' : 'Перейти к заявкам'} →</button>
        </section>
        <section class="stats-grid">${stats.map(([value, label, hint, icon]) => `
            <article class="stat-card"><div class="stat-top"><span>${label}</span><span class="stat-icon">${icon}</span></div><strong class="stat-value">${value}</strong><small>${hint}</small></article>`).join('')}
        </section>
        <section class="dashboard-grid">
            <article class="card">
                <div class="card-header"><h3>Последние заявки</h3><button class="link-button" data-action="go-applications">Все заявки →</button></div>
                ${apps.length ? applicationsTable(apps.slice(0, 5)) : emptyState('Заявок пока нет', 'Создайте первую заявку, выбрав услугу из каталога.', '▤')}
            </article>
            <article class="card"><div class="card-header"><h3>Как проходит работа</h3></div><div class="card-body process-list">
                ${[['Заявка', 'Выберите услугу и приложите исходные документы'], ['Проверка', 'Нотариус изучает документы и обновляет статус'], ['Оплата', 'Оплатите услугу через платёжную заглушку'], ['Получение', 'Подпишите и скачайте готовые документы']].map(([title, text], index) => `
                    <div class="process-item"><span class="process-number">0${index + 1}</span><div><strong>${title}</strong><p>${text}</p></div></div>`).join('')}
            </div></article>
        </section>`;
}

function applicationsTable(apps) {
    return `<div class="table-wrap"><table><thead><tr><th>Заявка</th>${state.role === 'NOTARY' ? '<th>Клиент</th>' : ''}<th>Дата создания</th><th>Статус</th><th></th></tr></thead><tbody>
        ${apps.map(app => `<tr><td><span class="cell-title">№${app.id} · ${esc(app.serviceName)}</span><span class="cell-subtitle">${esc(app.note || 'Без описания')}</span></td>${state.role === 'NOTARY' ? `<td>${esc(app.clientName)}</td>` : ''}<td>${formatDate(app.createdAt)}</td><td>${statusBadge(app.status, app.statusDisplay)}</td><td><div class="row-actions"><button class="button secondary small" data-action="view-application" data-id="${app.id}">Открыть</button></div></td></tr>`).join('')}
    </tbody></table></div>`;
}

async function renderApplications() {
    await loadApplications();
    const isClient = state.role === 'CLIENT';
    $('#page-content').innerHTML = `
        <div class="page-intro"><div><h3>${isClient ? 'Мои заявки' : 'Реестр заявок'}</h3><p>${isClient ? 'История обращений, документы и оплаты' : 'Обработка обращений и выдача документов'}</p></div>${isClient ? '<button class="button primary" data-action="new-application">+ Новая заявка</button>' : ''}</div>
        <section class="card">${state.applications.length ? applicationsTable(state.applications) : emptyState('Здесь появятся заявки', isClient ? 'Выберите нотариальную услугу и отправьте первую заявку.' : 'Клиенты ещё не создали ни одной заявки.', '▤')}</section>`;
}

async function renderServices() {
    await loadCatalog();
    const isNotary = state.role === 'NOTARY';
    $('#page-content').innerHTML = `
        <div class="page-intro"><div><h3>${isNotary ? 'Каталог и тарифы' : 'Нотариальные услуги'}</h3><p>${isNotary ? 'Создание, редактирование и удаление услуг' : 'Актуальные описания и стоимость услуг конторы'}</p></div>${isNotary ? '<button class="button primary" data-action="new-service">+ Добавить услугу</button>' : ''}</div>
        ${state.services.length ? `<div class="service-grid">${state.services.map((service, index) => `
            <article class="service-card"><div class="service-card-top"><span class="service-number">УСЛУГА ${String(index + 1).padStart(2, '0')}</span>${statusBadge('ready', service.pricingTypeDisplay)}</div>
            <h4>${esc(service.name)}</h4><p>${esc(service.description)}</p>
            <div class="service-card-footer"><div class="price">${servicePrice(service)}<small>${service.pricingType === 'PRIVILEGED' ? 'Для льготной категории' : 'Стоимость услуги'}</small></div>
            <div class="row-actions">${isNotary ? `<button class="button secondary small" data-action="edit-service" data-id="${service.id}">Изменить</button><button class="button danger small" data-action="delete-service" data-id="${service.id}">Удалить</button>` : `<button class="button ghost small" data-action="order-service" data-id="${service.id}">Выбрать →</button>`}</div></div></article>`).join('')}</div>`
        : emptyState('Каталог пуст', 'Нотариус ещё не добавил услуги.', '◇')}`;
}

async function renderNotifications() {
    const notifications = await api(`/api/notifications/${state.client.id}`);
    notifications.sort((a, b) => b.id - a.id);
    $('#page-content').innerHTML = `<div class="page-intro"><div><h3>История уведомлений</h3><p>Сообщения формируются автоматически при создании заявки и изменении статуса</p></div></div>
        ${notifications.length ? `<div class="notification-list">${notifications.map(item => `<article class="notification-item"><span class="notification-icon">✉</span><div><h4>${esc(item.type)}${item.applicationId ? ` · заявка №${item.applicationId}` : ''}</h4><p>${esc(item.message)}</p><small>${formatDate(item.createdAt)}</small></div><span class="notification-status">${esc(item.status)}</span></article>`).join('')}</div>` : emptyState('Уведомлений пока нет', 'После создания заявки здесь появится первое сообщение.', '✉')}`;
}

function renderProfile() {
    const client = state.client;
    $('#page-content').innerHTML = `<div class="page-intro"><div><h3>Личные данные</h3><p>Эти данные используются в заявках и для отправки уведомлений</p></div></div>
        <section class="card profile-card"><div class="card-header"><h3>Профиль клиента №${client.id}</h3></div><div class="card-body"><form id="profile-form" class="profile-form">
            <label>Фамилия, имя, отчество<input name="fullName" value="${esc(client.fullName)}" required></label>
            <div class="form-row"><label>Паспортные данные<input name="passportData" value="${esc(client.passportData)}" required></label><label>Телефон<input name="phone" value="${esc(client.phone)}"></label></div>
            <label>E-mail<input name="email" type="email" value="${esc(client.email)}" required></label>
            <label>Новый пароль<input name="password" type="password" minlength="6" placeholder="Оставьте пустым, чтобы не менять"></label>
            <div class="form-actions"><button class="button primary" type="submit">Сохранить изменения</button></div>
        </form></div></section>`;
    $('#profile-form').addEventListener('submit', updateProfile);
}

async function renderAudit() {
    const entries = await api('/api/audit');
    entries.sort((a, b) => b.id - a.id);
    $('#page-content').innerHTML = `<div class="page-intro"><div><h3>Журнал событий</h3><p>Автоматическая фиксация событий создания заявок и изменения их статусов</p></div></div>
        <section class="card">${entries.length ? `<div class="table-wrap"><table><thead><tr><th>ID</th><th>Событие</th><th>Описание</th><th>Дата и время</th></tr></thead><tbody>${entries.map(entry => `<tr><td>#${entry.id}</td><td><span class="cell-title">${esc(entry.eventType)}</span></td><td>${esc(entry.description)}</td><td>${formatDate(entry.createdAt)}</td></tr>`).join('')}</tbody></table></div>` : emptyState('Журнал пуст', 'События появятся после работы с заявками.', '≡')}</section>`;
}

function emptyState(title, text, icon) {
    return `<div class="empty-state"><span class="empty-icon">${icon}</span><h4>${esc(title)}</h4><p>${esc(text)}</p></div>`;
}

function openModal(title, html, eyebrow = 'Нотариальная контора') {
    $('#modal-title').textContent = title;
    $('#modal-eyebrow').textContent = eyebrow;
    $('#modal-body').innerHTML = html;
    if (!$('#modal').open) $('#modal').showModal();
}

function closeModal() {
    $('#modal').close();
}

function openNewApplication(serviceId = '') {
    openModal('Новая заявка', `<form id="new-application-form" class="modal-form">
        <label>Нотариальная услуга<select name="serviceId" required><option value="">Выберите услугу</option>${state.services.map(service => `<option value="${service.id}" ${String(service.id) === String(serviceId) ? 'selected' : ''}>${esc(service.name)} — ${servicePrice(service)}</option>`).join('')}</select></label>
        <label>Описание и необходимые данные<textarea name="note" placeholder="Например: доверенность на управление автомобилем, данные доверенного лица…" required></textarea></label>
        <label>Исходные документы<input class="file-input" name="documents" type="file" multiple><span class="field-hint">Можно выбрать несколько файлов, каждый размером до 8 МБ</span></label>
        <div class="form-actions"><button class="button secondary" type="button" data-action="close-modal">Отмена</button><button class="button primary" type="submit">Отправить заявку →</button></div>
    </form>`, 'Создание заявки');
    $('#new-application-form').addEventListener('submit', createApplication);
}

async function createApplication(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const button = form.querySelector('[type=submit]');
    button.disabled = true;
    try {
        const data = new FormData(form);
        const application = await api('/api/applications', { method: 'POST', body: { serviceId: Number(data.get('serviceId')), note: data.get('note') } });
        for (const file of form.documents.files) {
            await api(`/api/applications/${application.id}/documents`, { method: 'POST', body: { fileName: file.name, contentBase64: await fileBase64(file) } });
        }
        closeModal();
        toast(`Заявка №${application.id} создана`);
        await navigate('applications');
        await showApplication(application.id);
    } catch (error) {
        toast(error.message, true);
        button.disabled = false;
    }
}

function fileBase64(file) {
    return new Promise((resolve, reject) => {
        if (file.size > 8 * 1024 * 1024) return reject(new Error(`Файл ${file.name} превышает 8 МБ`));
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result.split(',')[1]);
        reader.onerror = () => reject(new Error('Не удалось прочитать файл'));
        reader.readAsDataURL(file);
    });
}

async function showApplication(id) {
    const app = await api(`/api/applications/${id}`);
    const isNotary = state.role === 'NOTARY';
    const isOwner = state.role === 'CLIENT';
    const service = state.services.find(item => item.id === app.serviceId) || await api(`/api/services/${app.serviceId}`);
    const documents = app.documents.length ? app.documents.map(doc => `
        <div class="document-row"><span class="document-icon">▧</span><div class="document-info"><strong>${esc(doc.fileName)}</strong><small>${esc(doc.typeDisplay)} · ${doc.signed ? 'Подписан' : 'Не подписан'}</small></div><div class="row-actions">
            <button class="button secondary small" data-action="download-document" data-id="${app.id}" data-doc-id="${doc.id}">Скачать</button>
            ${isOwner && app.status === 'READY' && doc.type === 'RESULT' && !doc.signed ? `<button class="button ghost small" data-action="sign-document" data-id="${app.id}" data-doc-id="${doc.id}">Подписать</button>` : ''}
            ${isOwner && app.status === 'CREATED' && doc.type === 'SOURCE' ? `<button class="button danger small" data-action="delete-document" data-id="${app.id}" data-doc-id="${doc.id}">Удалить</button>` : ''}
        </div></div>`).join('') : '<p class="muted">Документы ещё не добавлены.</p>';
    const payments = app.payments.length ? `<div class="table-wrap"><table><thead><tr><th>Платёж</th><th>Сумма</th><th>Статус</th><th>Дата</th></tr></thead><tbody>${app.payments.map(payment => `<tr><td>#${payment.id}</td><td>${money.format(payment.amount)}</td><td>${statusBadge(payment.status, payment.statusDisplay)}</td><td>${formatDate(payment.createdAt)}</td></tr>`).join('')}</tbody></table></div>` : '<p class="muted">Платежей по заявке ещё не было.</p>';
    const actions = [];
    if (isOwner && app.status === 'CREATED') actions.push(`<button class="button secondary" data-action="edit-application" data-id="${app.id}">Изменить описание</button>`);
    if (isOwner && ['CREATED', 'IN_PROGRESS'].includes(app.status)) actions.push(`<button class="button secondary" data-action="upload-document" data-id="${app.id}">+ Документ</button>`, `<button class="button danger" data-action="cancel-application" data-id="${app.id}">Отменить заявку</button>`);
    if (isOwner && !['RECEIVED', 'REJECTED', 'CANCELLED'].includes(app.status)) actions.push(`<button class="button primary" data-action="pay-application" data-id="${app.id}">Оплатить услугу</button>`);
    if (isOwner && app.status === 'READY') actions.push(`<button class="button primary" data-action="receive-application" data-id="${app.id}">Подтвердить получение</button>`);
    if (isNotary && app.status === 'CREATED') actions.push(`<button class="button primary" data-action="set-status" data-status="IN_PROGRESS" data-id="${app.id}">Взять в работу</button>`, `<button class="button danger" data-action="set-status" data-status="CANCELLED" data-id="${app.id}">Отменить</button>`);
    if (isNotary && app.status === 'IN_PROGRESS') actions.push(`<button class="button primary" data-action="set-status" data-status="READY" data-id="${app.id}">Документы готовы</button>`, `<button class="button danger" data-action="set-status" data-status="REJECTED" data-id="${app.id}">Отклонить</button>`, `<button class="button secondary" data-action="set-status" data-status="CANCELLED" data-id="${app.id}">Отменить</button>`);
    if (isNotary && app.status === 'READY') actions.push(`<button class="button primary" data-action="generate-document" data-id="${app.id}">+ Готовый документ</button>`);
    openModal(`Заявка №${app.id}`, `<div class="detail-heading"><div><h4>${esc(app.serviceName)}</h4><p>${esc(app.clientName)} · создана ${formatDate(app.createdAt)}</p></div>${statusBadge(app.status, app.statusDisplay)}</div>
        <div class="detail-grid"><div class="detail-field"><small>Услуга</small><strong>${esc(service.pricingTypeDisplay)}</strong></div><div class="detail-field"><small>Тариф</small><strong>${servicePrice(service)}</strong></div><div class="detail-field"><small>Номер заявки</small><strong>№${app.id}</strong></div></div>
        <div class="detail-field"><small>Описание заявки</small><strong>${esc(app.note || 'Не указано')}</strong></div>
        <section class="detail-section"><div class="detail-section-header"><h4>Документы (${app.documents.length})</h4></div>${documents}</section>
        <section class="detail-section"><div class="detail-section-header"><h4>История оплаты</h4></div>${payments}</section>
        ${actions.length ? `<div class="detail-actions">${actions.join('')}</div>` : ''}`, 'Карточка заявки');
}

function openEditApplication(id) {
    const app = state.applications.find(item => item.id === Number(id));
    openModal(`Изменение заявки №${id}`, `<form id="edit-application-form" class="modal-form"><label>Описание заявки<textarea name="note" required>${esc(app?.note)}</textarea></label><div class="form-actions"><button type="button" class="button secondary" data-action="close-modal">Отмена</button><button class="button primary" type="submit">Сохранить</button></div></form>`);
    $('#edit-application-form').addEventListener('submit', async event => {
        event.preventDefault();
        await api(`/api/applications/${id}`, { method: 'PUT', body: { note: event.currentTarget.note.value } });
        toast('Описание заявки обновлено');
        await refreshApplication(id);
    });
}

function openUpload(id) {
    openModal('Добавление документа', `<form id="upload-form" class="modal-form"><label>Файл<input class="file-input" name="document" type="file" required><span class="field-hint">Максимальный размер — 8 МБ</span></label><div class="form-actions"><button type="button" class="button secondary" data-action="close-modal">Отмена</button><button class="button primary" type="submit">Загрузить</button></div></form>`, `Заявка №${id}`);
    $('#upload-form').addEventListener('submit', async event => {
        event.preventDefault();
        const file = event.currentTarget.document.files[0];
        await api(`/api/applications/${id}/documents`, { method: 'POST', body: { fileName: file.name, contentBase64: await fileBase64(file) } });
        toast('Документ загружен');
        await refreshApplication(id);
    });
}

function openGenerate(id) {
    openModal('Готовый документ', `<form id="generate-form" class="modal-form"><label>Имя файла<input name="fileName" value="notarial-document-${id}.txt" required><span class="field-hint">Система сформирует документ с данными заявки</span></label><div class="form-actions"><button type="button" class="button secondary" data-action="close-modal">Отмена</button><button class="button primary" type="submit">Сформировать</button></div></form>`, `Заявка №${id}`);
    $('#generate-form').addEventListener('submit', async event => {
        event.preventDefault();
        await api(`/api/applications/${id}/documents/generate`, { method: 'POST', body: { fileName: event.currentTarget.fileName.value } });
        toast('Готовый документ сформирован');
        await refreshApplication(id);
    });
}

function openPayment(id) {
    const app = state.applications.find(item => item.id === Number(id));
    const service = state.services.find(item => item.id === app?.serviceId);
    const percentage = service?.pricingType === 'PERCENTAGE';
    openModal('Оплата услуги', `<form id="payment-form" class="modal-form"><div class="detail-field"><small>Услуга</small><strong>${esc(service?.name)}</strong></div>
        ${percentage ? '<label>Сумма сделки, ₽<input name="dealAmount" type="number" min="0.01" step="0.01" placeholder="5000000" required><span class="field-hint">Тариф составит 0,5% от указанной суммы</span></label>' : `<p class="muted">Будет создан платёж по тарифу «${servicePrice(service)}». Это учебная имитация оплаты, реальные деньги не списываются.</p>`}
        <div class="form-actions"><button type="button" class="button secondary" data-action="close-modal">Отмена</button><button class="button primary" type="submit">Оплатить →</button></div></form>`, `Заявка №${id}`);
    $('#payment-form').addEventListener('submit', async event => {
        event.preventDefault();
        const dealAmount = new FormData(event.currentTarget).get('dealAmount');
        const payment = await api(`/api/payment/${id}`, { method: 'POST', body: { dealAmount } });
        toast(`Платёж №${payment.id} выполнен: ${money.format(payment.amount)}`);
        await refreshApplication(id);
    });
}

function openServiceForm(id = null) {
    const service = id ? state.services.find(item => item.id === Number(id)) : null;
    openModal(service ? 'Редактирование услуги' : 'Новая услуга', `<form id="service-form" class="modal-form">
        <label>Название<input name="name" value="${esc(service?.name)}" required></label><label>Описание<textarea name="description" required>${esc(service?.description)}</textarea></label>
        <div class="form-row"><label>Базовый тариф, ₽<input name="price" type="number" min="0" step="0.01" value="${service?.price ?? ''}" required></label><label>Тип тарификации<select name="pricingType"><option value="FIXED" ${service?.pricingType === 'FIXED' ? 'selected' : ''}>Фиксированный</option><option value="PERCENTAGE" ${service?.pricingType === 'PERCENTAGE' ? 'selected' : ''}>Процент от сделки</option><option value="PRIVILEGED" ${service?.pricingType === 'PRIVILEGED' ? 'selected' : ''}>Льготный</option></select></label></div>
        <div class="form-actions"><button type="button" class="button secondary" data-action="close-modal">Отмена</button><button class="button primary" type="submit">Сохранить услугу</button></div></form>`, 'Управление каталогом');
    $('#service-form').addEventListener('submit', async event => {
        event.preventDefault();
        const values = new FormData(event.currentTarget);
        const body = { name: values.get('name'), description: values.get('description'), price: values.get('price'), pricingType: values.get('pricingType') };
        await api(id ? `/api/services/${id}` : '/api/services', { method: id ? 'PUT' : 'POST', body });
        closeModal();
        toast(id ? 'Услуга обновлена' : 'Услуга добавлена');
        await renderServices();
    });
}

async function updateProfile(event) {
    event.preventDefault();
    const values = new FormData(event.currentTarget);
    const body = Object.fromEntries(values.entries());
    state.client = await api(`/api/client/${state.client.id}`, { method: 'PUT', body });
    saveSession();
    $('#sidebar-name').textContent = state.client.fullName;
    $('#sidebar-email').textContent = state.client.email;
    toast('Профиль успешно обновлён');
}

async function refreshApplication(id) {
    await loadApplications();
    await showApplication(id);
}

async function runAction(action, target) {
    if (action === 'new-application') return openNewApplication();
    if (action === 'go-applications') return navigate('applications');
    if (action === 'close-modal') return closeModal();
    if (action === 'toggle-menu') return $('.sidebar').classList.toggle('open');
    if (action === 'logout') {
        try { await api('/api/logout', { method: 'POST' }); } catch (_) { /* Локальный выход выполняется в любом случае. */ }
        clearSession();
        showAuth();
        return;
    }
    if (action === 'view-application') return showApplication(target.dataset.id);
    if (action === 'order-service') return openNewApplication(target.dataset.id);
    if (action === 'edit-application') return openEditApplication(target.dataset.id);
    if (action === 'upload-document') return openUpload(target.dataset.id);
    if (action === 'generate-document') return openGenerate(target.dataset.id);
    if (action === 'pay-application') return openPayment(target.dataset.id);
    if (action === 'new-service') return openServiceForm();
    if (action === 'edit-service') return openServiceForm(target.dataset.id);
    if (action === 'delete-service') {
        if (!confirm('Удалить услугу из каталога?')) return;
        await api(`/api/services/${target.dataset.id}`, { method: 'DELETE' });
        toast('Услуга удалена');
        return renderServices();
    }
    const id = target.dataset.id;
    if (action === 'set-status') {
        await api(`/api/applications/${id}/status`, { method: 'PATCH', body: { status: target.dataset.status } });
        toast('Статус заявки изменён, уведомление отправлено');
        return refreshApplication(id);
    }
    if (action === 'cancel-application') {
        if (!confirm('Отменить заявку? Это действие нельзя отменить.')) return;
        await api(`/api/applications/${id}`, { method: 'DELETE' });
        toast('Заявка отменена');
        return refreshApplication(id);
    }
    if (action === 'sign-document') {
        await api(`/api/applications/${id}/documents/${target.dataset.docId}/sign`, { method: 'POST', body: {} });
        toast('Документ подписан');
        return refreshApplication(id);
    }
    if (action === 'delete-document') {
        if (!confirm('Удалить исходный документ?')) return;
        await api(`/api/applications/${id}/documents/${target.dataset.docId}`, { method: 'DELETE' });
        toast('Документ удалён');
        return refreshApplication(id);
    }
    if (action === 'receive-application') {
        await api(`/api/applications/${id}/receive`, { method: 'POST', body: {} });
        toast('Получение документов подтверждено');
        return refreshApplication(id);
    }
    if (action === 'download-document') return downloadDocument(id, target.dataset.docId);
}

document.addEventListener('click', async event => {
    const target = event.target.closest('[data-action]');
    if (!target) return;
    target.disabled = true;
    try {
        await runAction(target.dataset.action, target);
    } catch (error) {
        toast(error.message, true);
    } finally {
        if (document.contains(target)) target.disabled = false;
    }
});

async function downloadDocument(applicationId, documentId) {
    const response = await fetch(`/api/applications/${applicationId}/documents/${documentId}/download`, { headers: { Authorization: `Bearer ${state.token}` } });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.error || 'Не удалось скачать документ');
    }
    const blob = await response.blob();
    const disposition = response.headers.get('Content-Disposition') || '';
    const encodedName = disposition.match(/filename\*=UTF-8''([^;]+)/)?.[1];
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = encodedName ? decodeURIComponent(encodedName) : `document-${documentId}`;
    link.click();
    URL.revokeObjectURL(link.href);
}

$$('[data-auth-tab]').forEach(button => button.addEventListener('click', () => switchAuthTab(button.dataset.authTab)));
$$('[data-role]').forEach(button => button.addEventListener('click', () => {
    state.loginRole = button.dataset.role;
    $$('[data-role]').forEach(item => item.classList.toggle('active', item === button));
    const notary = state.loginRole === 'notary';
    $('#client-login-fields').hidden = notary;
    $('#notary-login-fields').hidden = notary;
    $('#client-login-fields input').required = !notary;
    $('#notary-login-fields input').required = notary;
}));

$('#login-form').addEventListener('submit', async event => {
    event.preventDefault();
    const button = event.currentTarget.querySelector('[type=submit]');
    button.disabled = true;
    try {
        const values = new FormData(event.currentTarget);
        const notary = state.loginRole === 'notary';
        const body = notary
            ? { login: values.get('login'), password: values.get('password') }
            : { email: values.get('email'), password: values.get('password') };
        const result = await api(notary ? '/api/notary/login' : '/api/client/login', { method: 'POST', body });
        state.token = result.token;
        state.role = result.role;
        state.client = result.client || null;
        saveSession();
        await showApp();
    } catch (error) {
        toast(error.message, true);
    } finally {
        button.disabled = false;
    }
});

$('#register-form').addEventListener('submit', async event => {
    event.preventDefault();
    const button = event.currentTarget.querySelector('[type=submit]');
    button.disabled = true;
    try {
        const body = Object.fromEntries(new FormData(event.currentTarget).entries());
        await api('/api/client/register', { method: 'POST', body });
        event.currentTarget.reset();
        switchAuthTab('login');
        $('#login-form').email.value = body.email;
        toast('Регистрация завершена. Теперь войдите в кабинет.');
    } catch (error) {
        toast(error.message, true);
    } finally {
        button.disabled = false;
    }
});

$('#modal').addEventListener('click', event => {
    if (event.target === $('#modal')) closeModal();
});

(async function init() {
    try {
        const saved = JSON.parse(localStorage.getItem('notarySession'));
        if (saved?.token) {
            state.token = saved.token;
            state.role = saved.role;
            state.client = saved.client;
            await showApp();
        }
    } catch (_) {
        clearSession();
        showAuth();
    }
})();
